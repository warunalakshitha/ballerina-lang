// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/system;
import ballerina/task;
import ballerina/time;

# Cache cleanup task starting delay in ms.
@final int CACHE_CLEANUP_START_DELAY = 0;

# Cache cleanup task invoking interval in ms.
@final int CACHE_CLEANUP_INTERVAL = 5000;

# Map which stores all of the caches.
map<Cache> cacheMap;

# Cleanup task which cleans the cache periodically.
task:Timer cacheCleanupTimer = createCacheCleanupTask();

# Represents a cache entry.
#
# + value - cache value
# + lastAccessedTime - last accessed time in ms of this value which is used to remove LRU cached values
type CacheEntry record {
    any value;
    int lastAccessedTime;
    !...
};

# Represents a cache.
public type Cache object {

    private int capacity;
    map<CacheEntry> entries;
    int expiryTimeMillis;
    private float evictionFactor;
    private string uuid;

    public new(expiryTimeMillis = 900000, capacity = 100, evictionFactor = 0.25) {
        // Cache expiry time must be a positive value.
        if (expiryTimeMillis <= 0) {
            error e = error("Expiry time must be greater than 0.");
            panic e;
        }
        // Cache capacity must be a positive value.
        if (capacity <= 0) {
            error e = error("Capacity must be greater than 0.");
            panic e;
        }
        // Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).
        if (evictionFactor <= 0 || evictionFactor > 1) {
            error e = error("Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).");
            panic e;
        }
        // We remove empty caches to prevent OOM issues. So in such scenarios, the cache will not be in the `cacheMap`
        // when we are trying to add a new cache entry to that cache. So we need to create a new cache. For that, keep
        // track of the UUID.
        uuid = system:uuid();
        cacheMap[uuid] = self;
    }

    # Checks whether the given key has an accociated cache value.
    #
    # + return - True if the given key has an associated value, false otherwise.
    public function hasKey(string key) returns (boolean) {
        return entries.hasKey(key);
    }

    # Returns the size of the cache.
    #
    # + return - The size of the cache
    public function size() returns (int) {
        return entries.length();
    }

    # Adds the given key, value pair to the provided cache.
    #
    # + key - value which should be used as the key
    # + value - value to be cached
    public function put(string key, any value) {
        // We need to synchronize this process otherwise concurrecy might cause issues.
        lock {
            int cacheCapacity = capacity;
            int cacheSize = entries.length();

            // If the current cache is full, evict cache.
            if (cacheCapacity <= cacheSize) {
                evict();
            }
            // Add the new cache entry.
            int time = time:currentTime().time;
            CacheEntry entry = { value: value, lastAccessedTime: time };
            entries[key] = entry;

            // If the UUID is not found, that means that cache was removed after being empty. So we need to create a
            // new cache with the current cache object.
            if (!cacheMap.hasKey(uuid)) {
                cacheMap[uuid] = self;
            }
        }
    }

    # Evicts the cache when cache is full.
    function evict() {
        int maxCapacity = capacity;
        float ef = evictionFactor;
        int numberOfKeysToEvict = <int>(maxCapacity * ef);
        // Get the above number of least recently used cache entry keys from the cache
        string[] cacheKeys = getLRUCacheKeys(numberOfKeysToEvict);
        // Iterate through the map and remove entries.
        foreach c in cacheKeys {
            // These cache values are ignred. So it is not needed to check the return value for the remove function.
            _ = entries.remove(c);
        }
    }

    # Returns the cached value associated with the given key. If the provided cache key is not found,
    # () will be returned.
    #
    # + key - key which is used to retrieve the cached value
    # + return - The cached value associated with the given key
    public function get(string key) returns any? {
        // Check whether the requested cache is available.
        if (!hasKey(key)) {
            return ();
        }
        // Get the requested cache entry from the map.
        CacheEntry? cacheEntry = entries[key];

        match cacheEntry {
            CacheEntry entry => {
                // Check whether the cache entry is already expired. Since the cache cleaning task runs in predefined intervals,
                // sometimes the cache entry might not have been removed at this point even though it is expired. So this check
                // gurentees that the expired cache entries will not be returened.
                int currentSystemTime = time:currentTime().time;
                if (currentSystemTime >= entry.lastAccessedTime + expiryTimeMillis) {
                    // If it is expired, remove the cache and return nil.
                    remove(key);
                    return ();
                }
                // Modify the last accessed time and return the cache if it is not expired.
                entry.lastAccessedTime = time:currentTime().time;
                return entry.value;
            }
            () => {
                return ();
            }
        }
    }

    # Removes a cached value from a cache.
    #
    # + key - key of the cache entry which needs to be removed
    public function remove(string key) {
        // Cache might already be removed by the cache clearing task. So no need to check the return value.
        _ = entries.remove(key);
    }

    # Returns all keys from current cache.
    #
    # + return - all keys
    public function keys() returns string[] {
        return entries.keys();
    }

    # Returns the key of the Least Recently Used cache entry. This is used to remove cache entries if the cache is
    # full.
    #
    # + return - number of keys to be evicted
    function getLRUCacheKeys(int numberOfKeysToEvict) returns (string[]) {
        // Create new arrays to hold keys to be removed and hold the corresponding timestamps.
        string[] cacheKeysToBeRemoved = [];
        int[] timestamps = [];
        string[] keys = self.entries.keys();
        // Iterate through the keys.
        foreach key in keys {
            CacheEntry? cacheEntry = entries[key];
            match cacheEntry {
                CacheEntry entry => {
                    // Check and add the key to the cacheKeysToBeRemoved if it matches the conditions.
                    checkAndAdd(numberOfKeysToEvict, cacheKeysToBeRemoved, timestamps, key, entry.lastAccessedTime);
                }
                () => {
                    // If the key is not found in the map, that means that the corresponding cache is already removed
                    // (possibly by a another worker).
                }
            }
        }
        // Return the array.
        return cacheKeysToBeRemoved;
    }
};

# Removes expired cache entries from all caches.
#
# + return - Any error which occured during cache expiration
function runCacheExpiry() returns error? {

    // We need to keep track of empty caches. We remove these to prevent OOM issues.
    int emptyCacheCount = 0;
    string[] emptyCacheKeys = [];

    // Iterate through all caches.
    foreach currentCacheKey, currentCache in cacheMap {

        // Get the expiry time of the current cache
        int currentCacheExpiryTime = currentCache.expiryTimeMillis;

        // Create a new array to store keys of cache entries which needs to be removed.
        string[] cachesToBeRemoved = [];

        int cachesToBeRemovedIndex = 0;
        // Iterate through all keys.
        foreach key, entry in currentCache.entries {

            // Get the current system time.
            int currentSystemTime = time:currentTime().time;

            // Check whether the cache entry needs to be removed.
            if (currentSystemTime >= entry.lastAccessedTime + currentCacheExpiryTime) {
                cachesToBeRemoved[cachesToBeRemovedIndex] = key;
                cachesToBeRemovedIndex = cachesToBeRemovedIndex + 1;
            }
        }

        // Iterate through the key list which needs to be removed.
        foreach currentKeyIndex in 0..<cachesToBeRemovedIndex {
            string key = cachesToBeRemoved[currentKeyIndex];
            // Remove the cache entry.
            _ = currentCache.entries.remove(key);
        }

        // If there are no entries, we add that cache key to the `emptyCacheKeys`.
        int size = currentCache.entries.length();
        if (size == 0) {
            emptyCacheKeys[emptyCacheCount] = currentCacheKey;
            emptyCacheCount += 1;
        }
    }

    // We iterate though all empty cache keys and remove them from the `cacheMap`.
    foreach emptyCacheKey in emptyCacheKeys {
        _ = cacheMap.remove(emptyCacheKey);
    }
    return ();
}

# Utility function to identify which cache entries should be evicted.
function checkAndAdd(int numberOfKeysToEvict, string[] cacheKeys, int[] timestamps, string key, int lastAccessTime) {
    string myKey = key;
    int myLastAccessTime = lastAccessTime;

    // Iterate while we count all values from 0 to numberOfKeysToEvict exclusive of numberOfKeysToEvict since the
    // array size should be numberOfKeysToEvict.
    foreach index in 0..<numberOfKeysToEvict {
        // If we have encountered the end of the array, that means we can add the new values to the end of the
        // array since we haven’t reached the numberOfKeysToEvict limit.
        if (cacheKeys.length() == index) {
            cacheKeys[index] = myKey;
            timestamps[index] = myLastAccessTime;
            // Break the loop since we don't have any more elements to compare since we are at the end
            break;
        } else {
            // If the timestamps[index] > lastAccessTime, that means the cache which corresponds to the 'key' is
            // older than the current entry at the array which we are checking.
            if (timestamps[index] > myLastAccessTime) {
                // Swap the values. We use the swapped value to continue to check whether we can find any place to
                // add it in the array.
                string tempKey = cacheKeys[index];
                int tempTimeStamp = timestamps[index];
                cacheKeys[index] = myKey;
                timestamps[index] = myLastAccessTime;
                myKey = tempKey;
                myLastAccessTime = tempTimeStamp;
            }
        }
    }
}

# Creates a new cache cleanup task.
#
# + return - cache cleanup task ID
function createCacheCleanupTask() returns task:Timer {
    (function () returns error?) onTriggerFunction = runCacheExpiry;
    task:Timer timer = new(onTriggerFunction, (), CACHE_CLEANUP_INTERVAL, delay = CACHE_CLEANUP_START_DELAY);
    timer.start();
    return timer;
}

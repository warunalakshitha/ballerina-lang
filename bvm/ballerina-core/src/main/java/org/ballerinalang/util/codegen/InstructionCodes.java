/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.util.codegen;

/**
 * Bytecode instructions of a compiled Ballerina program.
 *
 * @since 0.87
 */
public interface InstructionCodes {

    int NOP = 0;
    int ICONST = 2;
    int FCONST = 3;
    int SCONST = 4;
    int ICONST_0 = 5;
    int ICONST_1 = 6;
    int ICONST_2 = 7;
    int ICONST_3 = 8;
    int ICONST_4 = 9;
    int ICONST_5 = 10;
    int FCONST_0 = 11;
    int FCONST_1 = 12;
    int FCONST_2 = 13;
    int FCONST_3 = 14;
    int FCONST_4 = 15;
    int FCONST_5 = 16;
    int BCONST_0 = 17;
    int BCONST_1 = 18;
    int RCONST_NULL = 19;
    int BICONST = 20;
    int DCONST = 21;

    int IMOVE = 22;
    int FMOVE = 23;
    int SMOVE = 24;
    int BMOVE = 25;
    int RMOVE = 26;
    int BIALOAD = 28;
    int IALOAD = 29;
    int FALOAD = 30;
    int SALOAD = 31;
    int BALOAD = 32;
    int RALOAD = 33;
    int JSONALOAD = 34;

    int IGLOAD = 36;
    int FGLOAD = 37;
    int SGLOAD = 38;
    int BGLOAD = 39;
    int RGLOAD = 40;

    int CHNRECEIVE = 42;
    int CHNSEND = 43;

    int MAPLOAD = 44;
    int JSONLOAD = 45;

    int COMPENSATE = 46;

    int BIASTORE = 47;
    int IASTORE = 48;
    int FASTORE = 49;
    int SASTORE = 50;
    int BASTORE = 51;
    int RASTORE = 52;
    int JSONASTORE = 53;

    int BIAND = 55;
    int IAND = 56;
    int BIOR = 57;
    int IOR = 58;

    int IGSTORE = 59;
    int FGSTORE = 60;
    int SGSTORE = 61;
    int BGSTORE = 62;
    int RGSTORE = 63;

    int ERROR = 65;
    int PANIC = 66;
    int REASON = 67;
    int DETAIL = 68;

    int MAPSTORE = 69;
    int JSONSTORE = 70;

    int IADD = 71;
    int FADD = 72;
    int SADD = 73;
    int DADD = 74;

    int SCOPE_END = 75;
    int LOOP_COMPENSATE = 76;

    int XMLADD = 77;
    int ISUB = 78;
    int FSUB = 79;
    int DSUB = 80;
    int IMUL = 81;
    int FMUL = 82;
    int DMUL = 83;
    int IDIV = 84;
    int FDIV = 85;
    int DDIV = 86;
    int IMOD = 87;
    int FMOD = 88;
    int DMOD = 89;
    int INEG = 90;
    int FNEG = 91;
    int DNEG = 92;
    int BNOT = 93;

    int IEQ = 94;
    int FEQ = 95;
    int SEQ = 96;
    int BEQ = 97;
    int DEQ = 98;
    int REQ = 99;
    int REF_EQ = 100;

    int INE = 101;
    int FNE = 102;
    int SNE = 103;
    int BNE = 104;
    int DNE = 105;
    int RNE = 106;
    int REF_NEQ = 107;

    int IGT = 108;
    int FGT = 109;
    int DGT = 110;

    int IGE = 111;
    int FGE = 112;
    int DGE = 113;

    int ILT = 114;
    int FLT = 115;
    int DLT = 116;

    int ILE = 117;
    int FLE = 118;
    int DLE = 119;

    int REQ_NULL = 120;
    int RNE_NULL = 121;

    int BR_TRUE = 122;
    int BR_FALSE = 123;

    int GOTO = 124;
    int HALT = 125;
    int TR_RETRY = 126;
    int CALL = 127;
    int VCALL = 128;
    int FPCALL = 129;
    int FPLOAD = 130;
    int VFPLOAD = 131;

    int CLONE = 125;

    // Type Conversion related instructions
    int I2F = 132;
    int I2S = 133;
    int I2B = 134;
    int I2D = 135;
    int F2I = 136;
    int F2S = 137;
    int F2B = 138;
    int F2D = 139;
    int S2I = 140;
    int S2F = 141;
    int S2B = 142;
    int S2D = 143;
    int B2I = 144;
    int B2F = 145;
    int B2S = 146;
    int B2D = 147;
    int D2I = 148;
    int D2F = 149;
    int D2S = 150;
    int D2B = 151;
    int DT2JSON = 152;
    int DT2XML = 153;
    int T2MAP = 154;
    int T2JSON = 155;
    int MAP2T = 156;
    int JSON2T = 157;
    int XML2S = 158;

    int BILSHIFT = 159;
    int BIRSHIFT = 160;
    int ILSHIFT = 161;
    int IRSHIFT = 162;

    // Type cast
    int I2ANY = 163;
    int F2ANY = 164;
    int S2ANY = 165;
    int B2ANY = 166;
    int ANY2I = 168;
    int ANY2F = 169;
    int ANY2S = 170;
    int ANY2B = 171;
    int ANY2D = 172;
    int ANY2JSON = 173;
    int ANY2XML = 174;
    int ANY2MAP = 175;
    int ANY2STM = 176;
    int ANY2DT = 177;
    int ANY2SCONV = 178;
    int ANY2BI = 179;
    int BI2ANY = 180;
    int ANY2E = 181;
    int ANY2T = 182;
    int ANY2C = 183;
    int CHECKCAST = 184;

    int ANY2TYPE = 185;

    int LOCK = 186;
    int UNLOCK = 187;

    // Transactions
    int TR_BEGIN = 188;
    int TR_END = 189;

    int WRKSEND = 190;
    int WRKRECEIVE = 191;
    int FORKJOIN = 192;

    int AWAIT = 193;

    int MAP2JSON = 194;
    int JSON2MAP = 195;

    int IS_ASSIGNABLE = 196;
    int O2JSON = 197;

    int ARRAY2JSON = 198;
    int JSON2ARRAY = 199;

    int BINEWARRAY = 200;
    int INEWARRAY = 201;
    int FNEWARRAY = 202;
    int SNEWARRAY = 203;
    int BNEWARRAY = 204;
    int RNEWARRAY = 205;
    int LENGTHOF = 208;

    int NEWSTRUCT = 210;
    int NEWMAP = 212;
    int NEWTABLE = 215;
    int NEWSTREAM = 217;

    int NEW_INT_RANGE = 218;
    int ITR_NEW = 219;
    int ITR_HAS_NEXT = 220;
    int ITR_NEXT = 221;
    int INT_RANGE = 222;

    int I2BI = 223;
    int BI2I = 224;
    int BIXOR = 225;
    int IXOR = 226;
    int BACONST = 227;
    int IURSHIFT = 228;

    int IRET = 229;
    int FRET = 230;
    int SRET = 231;
    int BRET = 232;
    int DRET = 233;
    int RRET = 234;
    int RET = 235;

    int XML2XMLATTRS = 236;
    int XMLATTRS2MAP = 237;
    int XMLATTRLOAD = 238;
    int XMLATTRSTORE = 239;
    int S2QNAME = 240;
    int NEWQNAME = 241;
    int NEWXMLELEMENT = 242;
    int NEWXMLCOMMENT = 243;
    int NEWXMLTEXT = 244;
    int NEWXMLPI = 245;
    int XMLSEQSTORE = 246;
    int XMLSEQLOAD = 247;
    int XMLLOAD = 248;
    int XMLLOADALL = 249;
    int NEWXMLSEQ = 250;

    int TYPE_TEST = 251;
    int TYPELOAD = 252;

    int TEQ = 253;
    int TNE = 254;

    int INSTRUCTION_CODE_COUNT = 255;
}

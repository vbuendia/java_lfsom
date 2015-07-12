
float maxcolor = 255; 
int num_layers;

int momentoPres = 0;
int capaPres = -1;

String versionProg = "1.0.0";
int margenizda = 0;
int widthreal = 0;
int numcolumnas = 5;
int margenlado = 50;
int margenalto = 230;
int margenaltint = 20;
int margeninterno = 20;
int desplyact = 0;
int desplyact2 = 0;
int posmouseY, posmouseY2;
float tambloquey;
int nOcultos = 0;
boolean cargadoCluster = false;
boolean cargadoSubnet = false;
boolean modoCalculando = false;
boolean modoDragLayer = false;
boolean modoSeco = false; //sin animaciones
boolean loadFree = false;
String cargaFree;
int savedTime;
int passedTime;
int totalTime = 500;


int xOffLayer = 0;
int yOffLayer = 0;
int capaCluster = -1;
int capaSubnet = -1;
int capaDentro = -1;
int capaDibuSel = -1;

String xmlActual;

int posdeci = 2;
boolean menu = false;
int bandaInf = height-230;
int anchoInf = 230; 

int nCluster = 5;
int inicioCalc = 0;

String fPDFOutput="";
boolean PDFoutput = false;
boolean PDFtotal = false;

boolean hiddenPanner = true;

GButton btnInput;
GButton btnSelected;
GButton btnCloseMenu;

GButton btnCluster;
GButton btnPDF;
GButton btnIteration;

ArrayList listaCapas;

GPanel wIteration,pVisualization,pSelection;
GPanel pnlTrain,pnlMySOM;
GButton btCloseWIteration; 
GButton btDelete;

GCheckbox chSelection; 
GToggleGroup togGroup1; 
GOption ORadius; 
GOption OCluster; 
GOption OCustom; 
GLabel label1; 
GCustomSlider snRadius;


LFSGrowingSOM gsom;

int radiusSel = 1;
ArrayList<Integer[]> listaHexSel;
int SEL_RADIUS = 10;
int SEL_CLUSTER = 20;
int SEL_CUSTOM = 30;
int SEL_SUBNET = 40;
int selType = SEL_RADIUS; //tipo de seleccion

float[] listaHits = null;  

GCheckbox cbxBatch, cbxOnline;
GCheckbox cbxPCA, cbxRandom, cbxInter, cbxVector;
GCheckbox cbxGauss, cbxCutGauss, cbxBobble;  

float sinRad;
String tituloSOM = "";

GCheckbox cbxSetSize,cbxSetGrow,cbxSetHier,cbxSetGCHSOM;
GSlider2D slParamSom;
GCustomSlider sdnClusters,sdnColumn;

GLabel lbCurFile;
//GLabel lbwidthSom, lbheightSom;
GLabel lbnCluster;
PGraphics rec_pseudo;
PGraphics rec_arr;
PGraphics pg;

GSketchPad spad;

LFSExpProps propi;
LFSSelProps selPropi;
LFSSOMProperties props;

LFSData datos1 = null;
int cMapAct = 0;
ControlP5 cp5;
MultiList listSOM;
GDropList dErrIndex;
GDropList dColorMap;
Button bCancel;

HexMapDistancer HexDist;
GTextField gtIterations; 
GTextField gtSigma,gtLearning,gtCPU,gtTitle,gtNWidth;


GKnob kb;
boolean som_cargado = false;
boolean modoDrag = true;
boolean modoDrag2 = false;
boolean fijadoSelec = false; 
boolean dibujandoSelec = false;
ArrayList<Integer[]> listaDibuSelec = null;
TrainSelector experimento;

int tamMenuTrain = 385;
int tamMenuSOMs = 425;
int tamMenuVis= 380;
int tamMenuSel = 200;


int[] arrayPseudocolor = {color(10,0,170),color(79,255,255),color(25,255,25),color(255,255,79),color(170,0,10)};
//float[] rangosColor={0,0.35,0.5,0.65,1};
float[] rangosColor={0,(float) 0.35,(float) 0.5,(float) 0.65,1};

//Proporciones
int ancho = 0;
int anchosinmarg = 0;
float anchograf = 0;
int xfinpos = 0;
int topolx = 0;
int topoly = 0;
int rad = 200;
int constpropor = 100;
float finalpos = 0;
int yfinpos = 0;
float valradj = 0;
float valradi = 0;
float constescala=0;
float yfintotpos = 0;

int anchoLab = 300; 
int altoLab = 25;
public static int FILE = 1;
public static int SELEC = 2;
int tipo = this.FILE;
String directorio="";
boolean cancelado = false;


//Mapas de color
String[] colorMaps= {"137 0 0,141 0 0,145 0 0,149 0 0,153 0 0,157 0 0,161 0 0,165 0 0,170 0 0,174 0 0,178 0 0,182 0 0,186 0 0,190 0 0,194 0 0,198 0 0,202 0 0,206 0 0,210 0 0,214 0 0,218 0 0,222 0 0,226 0 0,230 0 0,234 0 0,238 0 0,242 0 0,246 0 0,250 0 0,255 0 0,255 4 0,255 8 0,255 12 0,255 16 0,255 20 0,255 24 0,255 28 0,255 32 0,255 36 0,255 40 0,255 44 0,255 48 0,255 52 0,255 56 0,255 60 0,255 64 0,255 68 0,255 72 0,255 76 0,255 80 0,255 85 0,255 89 0,255 93 0,255 97 0,255 101 0,255 105 0,255 109 0,255 113 0,255 117 0,255 121 0,255 125 0,255 129 0,255 133 0,255 137 0,255 141 0,255 145 0,255 149 0,255 153 0,255 157 0,255 161 0,255 165 0,255 170 0,255 174 0,255 178 0,255 182 0,255 186 0,255 190 0,255 194 0,255 198 0,255 202 0,255 206 0,255 210 0,255 214 0,255 218 0,255 222 0,255 226 0,255 230 0,255 234 0,255 238 0,255 242 0,255 246 0,255 250 0,255 255 0,250 255 4,246 255 8,242 255 12,238 255 16,234 255 20,230 255 24,226 255 28,222 255 32,218 255 36,214 255 40,210 255 44,206 255 48,202 255 52,198 255 56,194 255 60,190 255 64,186 255 68,182 255 72,178 255 76,174 255 80,170 255 85,165 255 89,161 255 93,157 255 97,153 255 101,149 255 105,145 255 109,141 255 113,137 255 117,133 255 121,129 255 125,125 255 129,121 255 133,117 255 137,113 255 141,109 255 145,105 255 149,101 255 153,97 255 157,93 255 161,89 255 165,85 255 170,80 255 174,76 255 178,72 255 182,68 255 186,64 255 190,60 255 194,56 255 198,52 255 202,48 255 206,44 255 210,40 255 214,36 255 218,32 255 222,28 255 226,24 255 230,20 255 234,16 255 238,12 255 242,8 255 246,4 255 250,0 255 255,0 250 255,0 246 255,0 242 255,0 238 255,0 234 255,0 230 255,0 226 255,0 222 255,0 218 255,0 214 255,0 210 255,0 206 255,0 202 255,0 198 255,0 194 255,0 190 255,0 186 255,0 182 255,0 178 255,0 174 255,0 170 255,0 165 255,0 161 255,0 157 255,0 153 255,0 149 255,0 145 255,0 141 255,0 137 255,0 133 255,0 129 255,0 125 255,0 121 255,0 117 255,0 113 255,0 109 255,0 105 255,0 101 255,0 97 255,0 93 255,0 89 255,0 85 255,0 80 255,0 76 255,0 72 255,0 68 255,0 64 255,0 60 255,0 56 255,0 52 255,0 48 255,0 44 255,0 40 255,0 36 255,0 32 255,0 28 255,0 24 255,0 20 255,0 16 255,0 12 255,0 8 255,0 4 255,0 0 255,0 0 250,0 0 246,0 0 242,0 0 238,0 0 234,0 0 230,0 0 226,0 0 222,0 0 218,0 0 214,0 0 210,0 0 206,0 0 202,0 0 198,0 0 194,0 0 190,0 0 186,0 0 182,0 0 178,0 0 174,0 0 170,0 0 165,0 0 161,0 0 157,0 0 153,0 0 149,0 0 145,0 0 141,0 0 137,0 0 133,0 0 129",
"0 0 0,1 1 1,2 2 2,3 3 3,4 4 4,5 5 5,6 6 6,7 7 7,8 8 8,9 9 9,10 10 10,11 11 11,12 12 12,13 13 13,14 14 14,15 15 15,16 16 16,17 17 17,18 18 18,19 19 19,20 20 20,21 21 21,22 22 22,23 23 23,24 24 24,25 25 25,26 26 26,27 27 27,28 28 28,29 29 29,30 30 30,31 31 31,32 32 32,33 33 33,34 34 34,35 35 35,36 36 36,37 37 37,38 38 38,39 39 39,40 40 40,41 41 41,43 43 43,44 44 44,45 45 45,46 46 46,47 47 47,48 48 48,49 49 49,50 50 50,51 51 51,52 52 52,53 53 53,54 54 54,55 55 55,56 56 56,57 57 57,58 58 58,59 59 59,60 60 60,61 61 61,62 62 62,63 63 63,64 64 64,65 65 65,66 66 66,67 67 67,68 68 68,69 69 69,70 70 70,71 71 71,72 72 72,73 73 73,74 74 74,75 75 75,76 76 76,77 77 77,78 78 78,79 79 79,80 80 80,81 81 81,82 82 82,83 83 83,85 85 85,86 86 86,87 87 87,88 88 88,89 89 89,90 90 90,91 91 91,92 92 92,93 93 93,94 94 94,95 95 95,96 96 96,97 97 97,98 98 98,99 99 99,100 100 100,101 101 101,102 102 102,103 103 103,104 104 104,105 105 105,106 106 106,107 107 107,108 108 108,109 109 109,110 110 110,111 111 111,112 112 112,113 113 113,114 114 114,115 115 115,116 116 116,117 117 117,118 118 118,119 119 119,120 120 120,121 121 121,122 122 122,123 123 123,124 124 124,125 125 125,126 126 126,128 128 128,129 129 129,130 130 130,131 131 131,132 132 132,133 133 133,134 134 134,135 135 135,136 136 136,137 137 137,138 138 138,139 139 139,140 140 140,141 141 141,142 142 142,143 143 143,144 144 144,145 145 145,146 146 146,147 147 147,148 148 148,149 149 149,150 150 150,151 151 151,152 152 152,153 153 153,154 154 154,155 155 155,156 156 156,157 157 157,158 158 158,159 159 159,160 160 160,161 161 161,162 162 162,163 163 163,164 164 164,165 165 165,166 166 166,167 167 167,168 168 168,170 170 170,171 171 171,172 172 172,173 173 173,174 174 174,175 175 175,176 176 176,177 177 177,178 178 178,179 179 179,180 180 180,181 181 181,182 182 182,183 183 183,184 184 184,185 185 185,186 186 186,187 187 187,188 188 188,189 189 189,190 190 190,191 191 191,192 192 192,193 193 193,194 194 194,195 195 195,196 196 196,197 197 197,198 198 198,199 199 199,200 200 200,201 201 201,202 202 202,203 203 203,204 204 204,205 205 205,206 206 206,207 207 207,208 208 208,209 209 209,210 210 210,211 211 211,213 213 213,214 214 214,215 215 215,216 216 216,217 217 217,218 218 218,219 219 219,220 220 220,221 221 221,222 222 222,223 223 223,224 224 224,225 225 225,226 226 226,227 227 227,228 228 228,229 229 229,230 230 230,231 231 231,232 232 232,233 233 233,234 234 234,235 235 235,236 236 236,237 237 237,238 238 238,239 239 239,240 240 240,241 241 241,242 242 242,243 243 243,244 244 244,245 245 245,246 246 246,247 247 247,248 248 248,249 249 249,250 250 250,251 251 251,252 252 252,253 253 253,255 255 255"};
String[] colorMapNames = {"Jet","Grayscale"};
String[] indexes = {"Quantization Err.","Topographic Err.","Kaski & Lagus"};
String fKaski,fQuan,fTopo;
String[] indexfiles = {};
String actualFolder = "";
int actualSOM = 0;
treeSOM mySOMs;
String[] somFolders = {};
String[] somNames = {};
int[] somFathers = {};


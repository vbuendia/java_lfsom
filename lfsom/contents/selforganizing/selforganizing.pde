
import g4p_controls.*;
import lfsom.experiment.TrainSelector;
import lfsom.experiment.structNet;
import lfsom.experiment.treeSOM;
import lfsom.properties.LFSExpProps;
import lfsom.properties.LFSSOMProperties;
import lfsom.properties.LFSSelProps;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import controlP5.*;
import lfsom.data.LFSData;
import lfsom.layers.metrics.HexMapDistancer;
import lfsom.models.LFSGrowingSOM;

import processing.pdf.*;

//*** FUNCIONES DE DESPLAZAMIENTO

public void keyReleased() {
  
 if (key == 'P'&&!PDFtotal) {
   PDFtotal=true;  
   fPDFOutput = G4P.selectOutput("PDF output");
   PDFtotal=(fPDFOutput!=null && !fPDFOutput.equals(""));
 }
 
  if (key == 'W') {
    clusterSelec();
 }
 

 if (key == 'Q') {
  exit();
 }
}


public void mousePressed() {

  // mouseEvent variable contains the current event information
  if (mouseEvent.getClickCount()==2) {
    //ClickCount 2
    if (capaDentro != -1 && capaDentro == capaSubnet) {
       LayerSom capa = (LayerSom) (listaCapas.get(capaSubnet));
       capa.compruebaAccesoSubnet();
    
    } else {
    fijadoSelec = !fijadoSelec;
     
    if (capaDentro!=-1 && (selPropi.getStrSel().equals("SelCustom"))) {
         dibujaSeleccionCustom();
    }
  }

  } else {
   //No es clickCount 2
       
       if (mouseButton == RIGHT) {
       
         //Se comprueba si es una subnet, y si es asi se carga su parent
         int padre = mySOMs.get(actualSOM).getNumPadre();
         if (padre>-1) {
           carga_som_selec(padre);         
         }
          
       }
       else
  
       if (dibujandoSelec) {
                Integer[] mxmy = new Integer[2];
                mxmy[0] = mouseX;
                mxmy[1] = mouseY;
                listaDibuSelec.add(mxmy);
           } 
    
  }  
  if (!menu && mouseX<20 && mouseY > height/3 && mouseY < height/3+80) {
     menu = true;
     actualizaMenus();
  }  
}


public void mouseMoved() {
  
   modoDrag = mouseY<bandaInf && (!menu || mouseX < 230);
   modoDrag2 = mouseY>=bandaInf;
   posmouseY = mouseY-desplyact;
   posmouseY2 = mouseY-desplyact2;
   
   if (mouseX<20 && mouseY > height/3 && mouseY < height/3+80)  cursor(HAND);
    else cursor(ARROW);
  
}

public void mouseDragged() {
  
  //modoDrag = !dibujandoSelec && ((mouseY<bandaInf && !pnl.isCollapsed()) || pnl.isCollapsed());
  modoDrag = !dibujandoSelec && mouseY<bandaInf && (!menu || mouseX > 230);
  modoDrag2 = !dibujandoSelec && mouseY>=bandaInf;
  
  if (modoDrag && !modoDragLayer) {
    if ( (mouseY-posmouseY < 0) && ( -1*(mouseY-posmouseY) < tambloquey * num_layers/numcolumnas + margenalto))
       desplyact = mouseY-posmouseY; 
  } if (modoDrag2 && !modoDragLayer) {
    if ( (mouseY-posmouseY2 < 0))
       desplyact2 = mouseY-posmouseY2;     
  }
   else {
      posmouseY = mouseY-desplyact;
      posmouseY2 = mouseY-desplyact2;
   }   
  
}

public void mouseWheel(MouseEvent event) {
  float e = event.getAmount(); 
  
  
  if (!keyPressed) {
  
  if (selPropi.getStrSel().equals("SelRadius") && selPropi.getEnableCell() &&  !fijadoSelec) {
    snRadius.setValue((int)(snRadius.getValueI()+e));
  }
  else {
  
  int candidato = desplyact - (int) e * 10;
  
  if ((candidato < 0) && ( -1*(candidato) < tambloquey * num_layers/numcolumnas + margenalto))
  desplyact = candidato; 
  }
  
  } else {
   if (keyCode==SHIFT) {
      sdnColumn.setValue((int)(sdnColumn.getValueI()+e));
   }
  }
  
}

//*** FIN FUNCIONES DE DESPLAZAMIENTO



public boolean sketchFullScreen() {
  return true;
}

void carga_props() {

  try {
      File f = new File(dataPath(actualFolder+"/"+xmlActual)+"props");
      if (f.exists()) 
         props = new LFSSOMProperties(dataPath(dataPath(actualFolder+"/"+xmlActual)+"props"));
    
        else props = new LFSSOMProperties();
  } catch (Exception e) { e.printStackTrace();}
  
}



void carga_selprops () {

  try {
      File f = new File(dataPath("SelProps.xml"));
      if (f.exists()) 
         selPropi = new LFSSelProps(dataPath("SelProps.xml"));
    
        else selPropi = new LFSSelProps();
     
  } catch (Exception e) { e.printStackTrace();}
  
}

void inicia_carpetas() {

 mySOMs = new treeSOM(dataPath("nets"));
 somFolders = mySOMs.lista_folders();
 somNames = mySOMs.lista_nombres();
 somFathers = mySOMs.lista_father();


}

void carga_expprops() {
 try {
 File f = new File(dataPath(actualFolder+"/ExpProps.xml"));
 if (f.exists()) {
    propi = new LFSExpProps(dataPath(actualFolder+"/ExpProps.xml"));
   //Se trata de cargar el nombre del experimento y los ficheros
   XML xml = loadXML(dataPath(actualFolder+"/ExpProps.xml"));
   XML[] xmlsom = xml.getChildren("atrib");
   for (int i=0; i< xmlsom.length;i++) {
        XML[] funcion = xmlsom[i].getChildren("funcion");
        XML[] valor = xmlsom[i].getChildren("valor");
        String funci = funcion[0].getContent();
        if (funci.equals("setExpName"))
          propi.setExpName(valor[0].getContent()); 
         if (funci.equals("setNetNames")) {
            indexes = valor[0].getContent().split(",");
            //dErrIndex.setItems(indexes,0);            
                        
         }
         if (funci.equals("setNetFiles")) {
            indexfiles = valor[0].getContent().split(",");
            xmlActual=indexfiles[0];            
         }
         
           
   }
 } else propi = new LFSExpProps();
   tituloSOM = propi.getExpName();
  propi.setDataPath(dataPath(""));
   } catch (Exception e) { e.printStackTrace();}
}

public void carga_som_selec(int redSelec) {
   actualFolder = mySOMs.get(redSelec).getNetFolder();
   //inicia_carpetas();
   actualSOM = redSelec;
   carga_expprops();
   desplyact = 0;
   carga_som();
   
   //carga_colormaps();
   //carga_selprops();
   dErrIndex.setItems(indexes,0);

}

void carga_som () {

try {  
  
File f = new File(dataPath(actualFolder+"/"+xmlActual));
  
if (f.exists()) {
  
 nOcultos = 0;
 capaPres=-1;
 capaDentro = -1;
 capaDibuSel = -1;
 som_cargado = false; 
   XML xml = null;
  if (loadFree)
      xml = loadXML(cargaFree);
   else
      xml = loadXML(dataPath(actualFolder+"/"+xmlActual));
 
 XML[] xmlsom = xml.getChildren("atrib");

 xmlsom = xml.getChildren("struct");
 topoly = xmlsom[0].getInt("topoly");
 topolx = xmlsom[0].getInt("topolx");
 HexDist = new HexMapDistancer(topolx,topoly);
 num_layers = xmlsom[0].getInt("num_layers");
 
 xmlsom = xml.getChildren("layer");
 
 float[] arrayValores_d,arrayColores; 

 arrayColores = new float[topoly*topolx];
 arrayValores_d = new float[topoly*topolx];
  
  int limitSup = 10;
  int limitInf = 3;
  if (topolx >= 8) limitSup = 6;
     else {
       limitSup = 10;
       limitInf = 5;
     }
  
sdnColumn.setLimits(limitInf,limitSup);
  


 //Tratamiento de los colores
 
 int numinterv = arrayPseudocolor.length;

 listaCapas = new ArrayList<LayerSom>();
     
 capaCluster = -1;
 capaSubnet = -1; 
 for (int k=0;k<num_layers;k++)
  {
    
    String[] strbl = xmlsom[k].getContent().trim().split(" ");
    arrayValores_d = new float[strbl.length];
    for (int w=0; w<strbl.length; w++)
       arrayValores_d[w] = Float.valueOf(strbl[w]);
    
     
    arrayColores = escalaColores(arrayValores_d,maxcolor);
    
    boolean muestra_cuad =  Boolean.valueOf(xmlsom[k].getString("show_cuad")); 
    boolean oculto = false;
    if (xmlsom[k].getString("name").equals("(Cluster)")) capaCluster = k;
    
    if (xmlsom[k].getString("name").equals("(Hits)")) listaHits = arrayValores_d;
    
    listaCapas.add(new LayerSom(k, xmlsom[k].getString("name"), topoly,topolx,arrayValores_d,arrayColores, k+1,muestra_cuad,oculto));
    
  }
  
  //Ahora, si la red tiene subredes, se crea una capa generada con los valores de las subredes 
   ArrayList<structNet> sRedes = mySOMs.subredesFrom(actualSOM, xmlActual);
  if (sRedes.size()>0) {
    capaSubnet = num_layers;
    num_layers++;
    arrayValores_d = mySOMs.layerDibuSubRedesFrom(actualSOM, xmlActual, topoly,topolx);
    arrayColores = escalaColores(arrayValores_d,maxcolor);
    listaCapas.add(new LayerSom(num_layers-1, "(Subnet navigation)", topoly,topolx,arrayValores_d,arrayColores, num_layers,false,false));
  }
  
  carga_props();
  
  if (!modoSeco) {
    
   if (num_layers<=12) numcolumnas = 4;
   
   if (num_layers<=9) numcolumnas = 3;
   
   int minCol = (int) sdnColumn.getStartLimit();
   
   if (numcolumnas<minCol) numcolumnas = minCol;
   
   sdnColumn.setValue(numcolumnas);
  
  }
  
  listaHexSel = new ArrayList<Integer[]>();
  fijadoSelec = false;
  
  if (selPropi.getStrSel().equals("SelCluster")) actualizaClusters();
  actualizaSubnets();
  som_cargado = true;
}

} catch (Exception e) {
}
}

public void setup() {
//Leeria el som
size(displayWidth,displayHeight,JAVA2D);

widthreal = width;
bandaInf = height-230;

hint(ENABLE_STROKE_PURE);
//frame.setTitle("Self Organizing Maps in your hand");

rad = 200;
sinRad = rad * sin(TWO_PI/6);
valradj = 2 * sinRad;
valradi = (  rad + cos(TWO_PI/6) * rad );

listaHexSel = new ArrayList<Integer[]>();


inicia_carpetas();
if (mySOMs.size()>0) {
      actualFolder = mySOMs.getLista().get(0).getNetFolder();
      actualSOM = 0;
    } else actualFolder = dataPath("");

carga_expprops();
carga_colormaps();
carga_selprops();

 
 //***************** GUI


   
 customize_gui_train();
 createGUIWIteration();
 createGUIPVisualization();
 createGUIPSelection();
 customize_gui_mysoms();
 calcPosMenus();
 modoCalculando = false;
 customize_kb();  
 listaDibuSelec = new ArrayList<Integer[]>();
  
 actualiza_color();
if (indexfiles.length>=3) {
     fKaski = indexfiles[0];
     fQuan = indexfiles[1];
     fTopo = indexfiles[2];
     }
 experimento = new TrainSelector();
 
 smooth();
 prepara_panelArr();
 savedTime = millis();
 versionProg = new TrainSelector().getVersionprog();
 println(versionProg);
}





public void draw() {
  
  

  
  
 if (!modoCalculando) { 
  
 background(254,242,222);
 
  
 if (som_cargado) {  
 

  if (PDFoutput||PDFtotal) {
    try {
     beginRecord(PDF, fPDFOutput); 
       
    } catch (Exception e) {
     
      G4P.showMessage(this, e.getMessage(), "PDF output",G4P.ERROR);
      exit();
      
      PDFoutput=false;
      PDFtotal=false;
    } 
  }
  kb.setVisible(false);
  bCancel.setVisible(false);
 
 

  int despMargen = 60;
  if (topolx < 7) {
    margenalto = 270;
    despMargen = 100;
    } else { 
     margenalto = 230;
     despMargen = 60;
    }
  
  //Valores de proporcionalidad 
  ancho = (widthreal - 2*margenlado)/numcolumnas - 2*margeninterno;
  anchosinmarg = (widthreal - 2*margenlado)/numcolumnas;
   //Tambien sabemos el ancho que va a ocupar el grafico
  xfinpos = (int) (topolx * valradj);
  
  anchograf = xfinpos+2*margeninterno; 
  constescala = anchograf/ancho;
  yfinpos = (int) (topoly * valradi);
  yfintotpos = (float) (yfinpos + 1.5*rad + 3*constpropor);
  tambloquey = 2*margenaltint+(yfintotpos)/constescala;
  finalpos = (xfinpos+ rad * sin(TWO_PI/6))/constescala;     
  
  pushMatrix();
  
  translate(margenizda,desplyact);
  textSize(50); fill(5);
  text(tituloSOM, 40,margenalto-despMargen);
   
  for (int i =0; i<num_layers; i++) {
   LayerSom capa = (LayerSom) (listaCapas.get(i));
   if (!capa.isSelected() && !capa.isOculto()) capa.dibuja_visible();
  }
  if (modoDragLayer) {
    LayerSom capa = (LayerSom) (listaCapas.get(capaPres));
    if (!capa.isOculto()) capa.dibuja_visible();
  }
  
  popMatrix();
  
  if (dibujandoSelec) {
     stroke(255);
     strokeWeight((float) 1);
     fill(80,200);
     beginShape();
      for (int i = 0; i < listaDibuSelec.size() ; i++)
         vertex(listaDibuSelec.get(i)[0],listaDibuSelec.get(i)[1]);
     vertex(mouseX,mouseY);    
     endShape(CLOSE);      
    }
    
  gestionDrags();
  gestionSelecHex();
  if (PDFoutput) {
    
   endRecord(); 
   G4P.showMessage(this, "PDF saved", "PDF output",G4P.PLAIN);
   PDFoutput=false;
  } 
  
 }
 
 } else {
  int calc = millis(); 
  background(204,192,172); 
 
 //Se esta calculando
  kb.setVisible(true);
  int numIter = experimento.getNumIter();
  long iteAct = experimento.getIteAct();
  float valkb = 0;
  if (numIter>0) valkb = 100*(float) iteAct/numIter;
  kb.setValue(valkb);  
  bCancel.setVisible(true);
  textSize(15);
  int difer = (int)((calc - inicioCalc)/1000);
  
  String diferTiem = String.format("%02d", difer/3600)+":"+String.format("%02d", (difer%3600)/60)+":"+String.format("%02d", (difer%60));
  text("Elapsed: "+diferTiem, width/2-textWidth("Elapsed: "+diferTiem)/2,  height/2+110);
  String texTrain = "Trained "+String.valueOf(iteAct)+" out of "+String.valueOf(numIter);
  text(texTrain, width/2-textWidth(texTrain)/2,  height/2+140);
  
  
 } 
 
 if (modoSeco) modoSeco = false;
 
 if (!modoCalculando) {
        dibuja_menu_izda();           
     }
 
 if (som_cargado && !modoCalculando) {
   
   if (hiddenPanner) dibuja_hidden_panel();
   dibuja_menu_izda();
 
   for (int i =0; i<num_layers; i++) {
   LayerSom capa = (LayerSom) (listaCapas.get(i));
   if (!capa.isSelected() && capa.isOculto()) capa.dibuja_hidden();
  }
  if (modoDragLayer) {
    LayerSom capa = (LayerSom) (listaCapas.get(capaPres));
    if (capa.isOculto()) capa.dibuja_hidden();
  }
 }
 
 
 //Ahora se pinta el panel 
 image(rec_arr,0,0,width,200);
 
 if (PDFtotal) {
   endRecord(); 
   G4P.showMessage(this, "PDF saved", "PDF output",G4P.PLAIN);
   PDFtotal=false;
 } 
 
 if (loadFree) {
   passedTime = millis() - savedTime;
   if (passedTime > totalTime) {
     modoSeco = true;
     carga_som();
     savedTime = millis();
   }
 }
 
 text(versionProg, width-textWidth(versionProg)-20,  height-10);
}

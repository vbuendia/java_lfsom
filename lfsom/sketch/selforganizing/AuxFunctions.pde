


public void loadFromSOMFile() {
    String[] datosFich = G4P.selectInputDif("Seleccione fichero XML con la red", "XML", "Ficheros XML");
    cargaFree = datosFich[0];
    if (cargaFree!=null) {
        loadFree = true;   
        modoSeco = true;     
   }
}

public void gestionSelecHex() {
  if (!fijadoSelec) {
  boolean enc = false;
  
  
  if (selPropi.getEnableCell() && !mousePressed && mouseY<bandaInf) {
     
     int i=0;
    
     capaDentro = -1;
     while (i<num_layers && !enc) {
       LayerSom capa = (LayerSom) (listaCapas.get(i));
       if (capa.intoLayer(mouseX,mouseY)) {
        capaDentro = i; 
        i--;
        enc=true;
        selPropi.setSelSubnet(capaDentro==capaSubnet);
        if (selPropi.isSelSubnet()) {
         capa.creaSeleccionSubnet();
        } else {
        if (selPropi.getStrSel().equals("SelRadius")) capa.creaSeleccionRadius();
        if (selPropi.getStrSel().equals("SelCluster") && cargadoCluster) capa.creaSeleccionCluster();
        if (selPropi.getStrSel().equals("SelCustom")) capa.creaSeleccionCustom();  
        }
       }
       i++;
     }
  }
  if (!enc) listaHexSel.clear();  
  if (capaDentro == -1) dibujandoSelec = false;
  }
}



public void actualizaRadius() {
  
  radiusSel = snRadius.getValueI();
  selPropi.setRadius(radiusSel);
}

public void calcMargenIzda() {
 
 if (!menu) margenizda = 0;
  else margenizda = 215; 
  
 widthreal = width - margenizda;

}



public void dibujaSeleccionCustom() {

  if (dibujandoSelec && listaDibuSelec.size()>0) {
          fijadoSelec = true;
         }
         
   dibujandoSelec = !dibujandoSelec;

   if (dibujandoSelec) {
            capaDibuSel = capaDentro;
            listaDibuSelec = new ArrayList<Integer[]>();
            fijadoSelec = false;
            Integer[] mxmy = new Integer[2];
            mxmy[0] = mouseX;
            mxmy[1] = mouseY;
            listaDibuSelec.add(mxmy);
    }  
}


public void gestionDrags() {
  
  if (!dibujandoSelec && mousePressed ) {
    
    boolean estaEnCapa = false;
    
    for (int i =0; i<num_layers; i++) {
      LayerSom capa = (LayerSom) (listaCapas.get(i));
      boolean oculto = capa.isOculto();
      
        if (capa.intoLayer(mouseX,mouseY) ) {
        
        estaEnCapa = true;

        if (momentoPres==0) {
          if ((mouseY > bandaInf && oculto)|| (mouseY <=bandaInf && !oculto)) {
            
          //Acabamos de apretar
          momentoPres = second()+60*minute()+3600*hour();
          capaPres = i;
          modoDragLayer = false;
          }
        } else {
          //Ya estabamos apretando
          
          //Estamos en modoDragLayer, moviendo la capa
          if (modoDragLayer) {
          
            //Ahora puede ser que nos encontremos en 2 capas, la capa que estamos moviendo 
            // o la que queremos desplazar.
            
            if (i!=capaPres) {
             LayerSom capaPresD = (LayerSom) (listaCapas.get(capaPres));
             int posPres = capaPresD.getPosCuadProvis();
             boolean ocultoPres = capaPresD.isOculto();
            
            if (ocultoPres == oculto) {
             //Estamos en una capa para desplazar
             int posi = capa.getPosCuad();
             for (int k=0; k<num_layers; k++)
              if (k!=capaPres) {
                LayerSom capadespla = (LayerSom) (listaCapas.get(k));
                int posdespla = capadespla.getPosCuad();
                if (posi>posPres) {
                  if (posdespla<=posi && posdespla>posPres) capadespla.setPosCuad(posdespla-1);
                } else {
                  if (posdespla>=posi && posdespla<posPres) capadespla.setPosCuad(posdespla+1);
                }                

              }
              
              capaPresD.setPosCuadProvis(posi);
            }
              
            }
            
            LayerSom capaPresD = (LayerSom) (listaCapas.get(capaPres));
            capaPresD.setOculto(mouseY > bandaInf);
            
            
          } else 
          //No estamos en modoDragLayer, se comprueba el tiempo apretando la misma
          {
            int momentoAct = second()+60*minute()+3600*hour();
            //Si se cambia de capa, comienza de nuevo
            if (capaPres!=i && ((mouseY >= 230 && oculto) || (mouseY < 230 && !oculto))) {
              capaPres = i;
              momentoPres = momentoAct;
            }
            //Si llevamos los segundos necesarios, se selecciona la capa
            if ((momentoAct - momentoPres > 1.5)) {
                 capa.setSelected(true);
                 xOffLayer = mouseX;
                 yOffLayer = mouseY;
                 modoDragLayer = true;
                 capa.setPosCuadProvis(capa.getPosCuad());
                 
            }
             
          }
         
        }
    } else if (!modoDragLayer) capa.setSelected(false);
      
      
    }//fin del bucle de capas
    if (!estaEnCapa && !modoDragLayer) {
      momentoPres = 0;
      capaPres = 0;
    }
    
   } else {
    //No se ha pulsado el raton, o se ha soltado
    for (int i =0; i<num_layers; i++) {
      LayerSom capa = (LayerSom) (listaCapas.get(i));
       capa.setSelected(false);
    }
    if (modoDragLayer) {
     //Se pasa de provisional a definitivo
     LayerSom capaPresD = (LayerSom) (listaCapas.get(capaPres));
     capaPresD.setPosCuad(capaPresD.getPosCuadProvis());
    }
    modoDragLayer = false;
    momentoPres = 0;
    capaPres = 0;
   }
   
  }


 public void actualizaSubnets() {
   if (capaSubnet>-1) {
       LayerSom capa = (LayerSom) (listaCapas.get(capaSubnet));
       cargadoSubnet=false;
       HexDist.cargaSubnets(capa.getValores());
       cargadoSubnet=true;
    }
 }

 public void actualizaClusters() {
   if (capaCluster>-1) {
   LayerSom capa = (LayerSom) (listaCapas.get(capaCluster));
   cargadoCluster=false;
   HexDist.cargaClusters(capa.getValores());
   cargadoCluster=true;
   }
 }



  int calculaPseudocolor(float colorfill) {
    
     int i=1;
     
     while (colorfill>rangosColor[i] && i < rangosColor.length-1) i++;
  
     int arrini = arrayPseudocolor[i-1];
     int arrfin = arrayPseudocolor[i];
     
     int colordevuelve = lerpColor(arrini,arrfin,(colorfill-rangosColor[i-1])/(rangosColor[i]-rangosColor[i-1]));
     return(colordevuelve);    
  }
  
  
 public void clusteriza() {
 //Si no estaban los datos cargados en RAM, se cargan
 try {
  if (datos1 == null) datos1 = new LFSData(dataPath(actualFolder+"/data.csv"));
    kb.setVisible(true);
    kb.setValue(0);  
    gsom = new LFSGrowingSOM(dataPath(actualFolder+"/"+xmlActual),datos1);
    kb.setValue((float) 0.25);  
    gsom.clusteriza(nCluster);
    kb.setValue((float) 0.75);  
    gsom.EscribeXML(dataPath(actualFolder+"/"+xmlActual));
    kb.setValue((float) 0.99);  
    modoSeco = true;
    carga_som();
    kb.setVisible(false);
 
 } catch (Exception e) {
    e.printStackTrace();
 }
 
 
 }
 
 
 public void clusterSelec() {
 //Genera un cluster a partir de lo seleccionado.
 try {
   if (datos1 == null) datos1 = new LFSData(dataPath(actualFolder+"/data.csv"));
    kb.setVisible(true);
    kb.setValue(0);  
    gsom = new LFSGrowingSOM(dataPath(actualFolder+"/"+xmlActual),datos1);
    kb.setValue((float) 0.25);  
    gsom.clusterSelec(HexDist.traspon(HexDist.getIncluidos()));
    //gsom.clusteriza(nCluster);
    kb.setValue((float) 0.75);  
    gsom.EscribeXML(dataPath(actualFolder+"/"+xmlActual));
    kb.setValue((float) 0.99);  
    modoSeco = true;
    carga_som();
    kb.setVisible(false);
 
 } catch (Exception e) {
    e.printStackTrace();
 }
 
 
 
 }
  
  
 void carga_colormaps() {
 try {
      File f = new File(dataPath("colormaps.xml"));
      if (f.exists()) {
         XML xml = loadXML(dataPath("colormaps.xml"));
         XML[] xmlmap = xml.getChildren("colormap");
         colorMaps = new String[xmlmap.length];
         colorMapNames = new String[xmlmap.length];
         
         for (int k=0; k < xmlmap.length; k++) {
           colorMapNames[k] = xmlmap[k].getString("name");
           colorMaps[k] = xmlmap[k].getContent();
         }
      }
  } catch (Exception e) { e.printStackTrace();}


}


int[] getXYhidden(int posicion) {
  
  posicion = -1 * posicion;
  
  int margenArrTot = 15;
  int margenArr = 15;
  int margenIzTot = 25;
  int margenLado = 10;
  int[] dest = new int[2];
  int numc = (width-2*margenIzTot)/(anchoLab+margenLado);
  
  dest[0] = (int) margenIzTot + ((posicion-1) % numc) * (margenLado + anchoLab);
  dest[1] = (int) margenArrTot + ((posicion-1) / numc) * (margenArr + altoLab) + height-230;
  
  return dest;
}

void actualiza_color() {
//Se toma el color del primer colormap (TO DO: colormap seleccionado)
// y se vuelve a cargar el mapa del XML.
String[] arrColores = colorMaps[cMapAct].split(",");
arrayPseudocolor = new int[arrColores.length];
rangosColor = new float[arrColores.length];
float increm = (float) (1.0/arrColores.length);
for (int k=0; k< arrColores.length; k++) {
  String[] colAct = arrColores[k].split(" ");
  int m = parseInt(colAct[2]);
  arrayPseudocolor[arrColores.length-k-1] = color(parseInt(colAct[0]),parseInt(colAct[1]),parseInt(colAct[2]));
  rangosColor[k]=k*increm;  
}
float kini = 0;
float kfin = (xfinpos+rad * sin(TWO_PI/6));
int numinterv = arrayPseudocolor.length;
int[] recuadro_pseudo = new int[(int)kfin-(int)kini+1];

rec_pseudo = createGraphics((int)kfin-(int)kini+1,constpropor);
rec_pseudo.beginDraw();

for (int k= (int)kini;k<=(int)kfin;k++) {
  int kact = k-(int) kini;  
  recuadro_pseudo[kact] = (calculaPseudocolor(1-(k-kini)/(kfin-kini)));
  rec_pseudo.stroke(recuadro_pseudo[kact]);
  rec_pseudo.line(kfin-kact,0,kfin-kact,constpropor);       
}   

rec_pseudo.endDraw();
carga_som();

}


public void dibuja_menu_izda() {
 if (!menu) {
   
  stroke(10);fill(240); 
  
  rect(0,height/3,20,80,0,10,10,0);
  pushMatrix();
    
  
  textSize(12);
  translate(13,height/3+3*textWidth("MENU")/2+5);
  rotate(-HALF_PI);
  
  fill(10); 
  text("MENU",0,0);
  popMatrix();
  
 } else {
  
  stroke(40); fill(240); 
  rect(5,100,230,bandaInf-115,20,20,0,20); 
  line(5,125,235,125);
  textSize(14); 
  fill(10);
  text("OPTIONS",225/2-textWidth("OPTIONS")/2,117);
  
 }


}

public void actualizaMenus() {

   calcMargenIzda();  
   if (!menu) {
     pnlTrain.setCollapsed(true);
     pSelection.setCollapsed(false);
     pVisualization.setCollapsed(true);
     pnlMySOM.setCollapsed(true);
   }
   pnlTrain.setVisible(menu);
   pVisualization.setVisible(menu);
   pSelection.setVisible(menu);     
   pnlMySOM.setVisible(menu);   
   btnCloseMenu.setVisible(menu);
   calcPosMenus();
}


public void prepara_panelArr() {
     PImage img = loadImage(dataPath("img/MIVIUV-22100.png"));
     rec_arr = createGraphics(width,200);
     rec_arr.beginDraw();
     rec_arr.fill(254,242,222,210);
     //rec_arr.fill(254,242,222);
     rec_arr.noStroke();
     rec_arr.rect(0, 0, width, 95);
     rec_arr.stroke(45);
     
     rec_arr.fill(255);
     //rec_arr.setFont(dataPath("/fonts/AGaramondPro-BoldItalic-20.vlw"));
     rec_arr.rect(5, 5, width-15, 90,15);
     rec_arr.image(img,45,10);
     
     rec_arr.endDraw();

}

public void calcPosMenus() {
  
  int inicial = 140;
  int inc;
  
  pSelection.moveTo(5,inicial); 
  
  if (!pSelection.isCollapsed()) inicial += 5+tamMenuSel;
   else inicial += 25;
  
  pVisualization.moveTo(5,inicial);
  
  if (!pVisualization.isCollapsed()) inicial += 5+tamMenuVis;
   else inicial += 25;
  
  
  pnlTrain.moveTo(5,inicial);
  
  if (!pnlTrain.isCollapsed()) inicial += 5+tamMenuTrain;
   else inicial += 25;
  
  pnlMySOM.moveTo(5,inicial);
  
  if (pnlMySOM.isCollapsed()) {
    listSOM.setVisible(false);
    quita_listSOM();
  } else {
    //listSOM.setAbsPosition(new PVector(5, inicial+45));
    update_soms(15,inicial+65);
    
  }
  
  //if (!pnlTrain.isCollapsed()) inicial += 5+tamMenuTrain;
  // else inicial += 25;
     
}



 public boolean deleteFile(File fileToDelete) {
        if (fileToDelete == null || !fileToDelete.exists()) {
            return true;
        } else {            boolean result = deleteChildren(fileToDelete);
            result &= fileToDelete.delete();
            return result;
        }
    }
    public  boolean deleteChildren(File parent) {
        if (parent == null || !parent.exists())
            return false;
        boolean result = true;
        if (parent.isDirectory()) {
            File files[] = parent.listFiles();
            if (files == null) {
                result = false;
            } else {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.getName().equals(".") || file.getName().equals(".."))
                        continue;
                    if (file.isDirectory())
                        result &= deleteFile(file);
                    else
                        result &= file.delete();
                }

            }
        }
        return result;
    }


   float[] escalaColores(float[] aValores, float mColor) {
     float[] eColor = new float[aValores.length];
     float maximo_d = max(aValores);
     float minimo_d = min(aValores);
     float difer = maximo_d-minimo_d;
   
     for (int w=0;w<aValores.length;w++) {
      //println(num_layers+" "+k+" "+w);
      eColor[w]= mColor * (aValores[w]-minimo_d) / difer;    
    } 
    return eColor;
   }


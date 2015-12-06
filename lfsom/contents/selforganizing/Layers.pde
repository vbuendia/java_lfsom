//CLASE LAYERSOM ******************************************************

class LayerSom {
 int idLayer,filas,cols,numValores,poscuad,poscuadprovis;
 float destX,destY,actX = 0,actY = 0, despla = 50;
 boolean activa;
 float maxvalor,minvalor,mediovalor;
 float[] valores,colores;
 Hexagon[] gridValores;
 String nombre;
 boolean dragging = false;
 boolean selected = false;
 boolean oculto = false;
 boolean muestra_cuad;
 boolean enTransicion = false;
 int anchoTransi = 400;
 int altoTransi = 400;
 
 LayerSom (int idLayer, String nombre, int filas,int cols, float[] valores, float[] colores, int poscua, boolean muestra_c, boolean ocult) {
   
   this.oculto = ocult;
   this.idLayer=idLayer;
   this.nombre=nombre;
   this.filas=filas;
   this.cols=cols;
   this.numValores = this.filas*this.cols;
   this.valores=valores;
   this.colores=colores;
   this.maxvalor = max(valores);
   this.minvalor = min(valores);
   this.mediovalor = (this.maxvalor +this.minvalor)/2;
   
   this.maxvalor = (int) (100*this.maxvalor);
   this.minvalor = (int) (100*this.minvalor);
   this.mediovalor = (int) (100*this.mediovalor);
   
   this.maxvalor = this.maxvalor/100;
   this.minvalor = this.minvalor/100;
   this.mediovalor = this.mediovalor/100;
   
   long multipli = 1000;
   while (this.maxvalor == this.minvalor && multipli < 1000000) {
      this.maxvalor = max(valores);
      this.minvalor = min(valores);
      this.mediovalor = (this.maxvalor +this.minvalor)/2;
      this.maxvalor = (int) (multipli*this.maxvalor);
      this.minvalor = (int) (multipli*this.minvalor);
      this.mediovalor = (int) (multipli*this.mediovalor);
   
      this.maxvalor = this.maxvalor/multipli;
      this.minvalor = this.minvalor/multipli;
      this.mediovalor = this.mediovalor/multipli;
      multipli = multipli*10;
   }
   
   this.poscuad = poscua;

   this.muestra_cuad = muestra_c;
  
   gridValores = new Hexagon[filas*cols];
   int colact=0;
   for (int i = 0; i < filas; i++){
   for (int j = 0; j < cols; j++){
            
            
              int px;
              if ((i % 2) == 0) px = (int) (j * valradj);     
               else px = (int) ( j * valradj + sinRad);
              
               gridValores[i*cols+j] = new Hexagon(px, i* valradi, rad,colores[colact++],valores[j+i*cols]);
            }
          }

 }
 
 public float[] getValores() {
  return valores;
 }
 
 public void setPosCuadProvis (int pos) {
   this.poscuadprovis = pos;
 }
 
 public int getPosCuadProvis() {
   return this.poscuadprovis;
 }
 
 public void setPosCuad (int pos) {
   this.poscuad = pos;
 }
 
 public int getPosCuad() {
   return this.poscuad;
 }
 
 public void setSelected (boolean sel) {
  this.selected = sel;
  
 }
 
 
   void creaSeleccionSubnet() {
  //en principio selType = SEL_RADIUS
    
  //Se apura al hexagono seleccionado
   int i=0;
   boolean enc = false;
    while (i<this.filas*this.cols && !enc) {
     if (this.gridValores[i].intoHex(mouseX,mouseY,actX,actY) && HexDist!= null && cargadoSubnet) {
           enc = true;
           listaHexSel = HexDist.listaSubnet_display(i);
         };
     i++;  
   }    
   
   
 }
 
 
public void compruebaAccesoSubnet() {

 int i=0;
 int redSelec = -1;
 boolean enc = false;
  while (i<this.filas*this.cols && !enc) {
   if (this.gridValores[i].intoHex(mouseX,mouseY,actX,actY)) {
         enc = true;
         redSelec = mySOMs.subRedFromPos(actualSOM,xmlActual,i,this.filas,this.cols);
          println (redSelec);         
       };
   i++;  
 }    
 
 if (enc) {
   carga_som_selec(redSelec);
 }
  
}
 
  void creaSeleccionCluster() {
  //en principio selType = SEL_RADIUS
    
  //Se apura al hexagono seleccionado
   int i=0;
   boolean enc = false;
    while (i<this.filas*this.cols && !enc) {
     if (this.gridValores[i].intoHex(mouseX,mouseY,actX,actY) && HexDist!= null && cargadoCluster) {
           enc = true;

           listaHexSel = HexDist.listaCluster_display(i);
         };
     i++;  
   }    
   
   
 }
 
 
 
  void creaSeleccionCustom() {
  //en principio selType = SEL_RADIUS
    
  //Se apura al hexagono seleccionado
   int i=0;
   ArrayList <Integer> listaSel = new ArrayList <Integer>();
    while (i<this.filas*this.cols) {
     if (!this.gridValores[i].coincideColor(actX,actY)) {
           listaSel.add(i);
         };
     i++;  
   }    
   
   listaHexSel = HexDist.listaCustom_display(listaSel);
 }
 
 
 
 
 
 void creaSeleccionRadius() {
 
   int i=0;
   boolean enc = false;
    while (i<this.filas*this.cols && !enc) {
     if (this.gridValores[i].intoHex(mouseX,mouseY,actX,actY)) {
           enc = true;
           listaHexSel = HexDist.listaProximos_display(i,radiusSel);
         };
     i++;  
   }    
   
   
 }
 
 
 public boolean isSelected() {
   return this.selected;
 }
 
 public boolean intoLayer(int x, int y) {
   boolean dentro = false;
   
   if (!this.oculto) {
     if ((destX == actX) && (destY == actY)) {
     float proporcion = ancho/anchograf;
     if (x-margenizda>=proporcion*(actX-sinRad) && x-margenizda<=(actX+xfinpos)*proporcion) {
       if (y-desplyact>=proporcion*(actY-sinRad) && y-desplyact<=(actY+yfinpos)*proporcion) {
         dentro = true;
       }
     }
     }
   } else {
    dentro = (mouseX >= actX) && (mouseX <= actX + anchoLab) && (mouseY >= actY) && (mouseY <= actY + altoLab);
   }
   if (!this.isSelected() && (actX != destX || actY != destY)) dentro = false;
   return dentro;
 }
 
 
 
 public int totalHits() {
  int sumaHits = 0;
  
  for (int i=0; i<listaHits.length; i++) 
    sumaHits = sumaHits + (int) listaHits[i];
   
  return sumaHits;  
 }


 public float sumaHits() {
  
  float sumaHits = 0;
  
  for (int i=0; i<listaHexSel.size(); i++) {
    int item = listaHexSel.get(i)[0];
    sumaHits = sumaHits + listaHits[item];
  }
  return sumaHits;
 }
 
 public float weightMean() {
  float m = 0;
  float sumaHits = 0;
  
  for (int i=0; i<listaHexSel.size(); i++) {
    int item = listaHexSel.get(i)[0];
    sumaHits = sumaHits + listaHits[item];
    m = m + valores[item]*listaHits[item];
  }
  return m/sumaHits;
 
 }
 
 
 void setOculto (boolean ocul) {
   
   if (!this.oculto && ocul) {
     nOcultos++; 
     
     for (int k=0; k<num_layers; k++)
       {       
         LayerSom capadespla = (LayerSom) (listaCapas.get(k));
         int posdespla = capadespla.getPosCuad();
         if (posdespla > 0 && posdespla>this.poscuadprovis) capadespla.setPosCuad(posdespla-1);
       }        
     this.setPosCuadProvis(-1 * nOcultos);
     actX = mouseX;
     actY = mouseY;
     this.setTransicion(true);
    
   }
   
   if (this.oculto && !ocul) {
     nOcultos--;
     for (int k=0; k<num_layers; k++)
       {       
         LayerSom capadespla = (LayerSom) (listaCapas.get(k));
         int posicion = capadespla.getPosCuad();
         //println(this.poscuad);
         if (posicion<0 && abs(posicion)>abs(this.poscuadprovis)) capadespla.setPosCuad(posicion+1);
       }        
     this.setPosCuadProvis(num_layers-nOcultos);
     this.setPosCuad(num_layers-nOcultos);
     poscuad=num_layers-nOcultos;
     destX = (margenlado+margeninterno+(anchosinmarg*((poscuad-1)%numcolumnas)));
     destY = (margenalto+(margenaltint+tambloquey)*( (int) ((poscuad-1)/numcolumnas)));
     xOffLayer = (int) (destX);
     yOffLayer = (int) (destY+desplyact);
     actX = destX;
     actY = destY;
     modoSeco = true;
     
   }
   this.oculto = ocul;
 }
 
 public boolean isOculto() {
  return this.oculto;
 }
 
 public void setTransicion (boolean transi) {
   if (transi) {
     anchoTransi = 300;
     altoTransi=300;
   }
   this.enTransicion = transi;
   
 }
 
 void dibuja_transiHidden() {
   
    if (altoTransi<=altoLab) setTransicion(false);
    anchoTransi = anchoTransi-((anchoTransi-anchoLab)/2)-1;
    altoTransi = altoTransi-((altoTransi-altoLab)/2)-1;
    fill(254,242,222);
    rect(actX,actY+desplyact2,anchoTransi,altoTransi); 
    textSize(15); fill(10);
    text(this.nombre,actX+5,actY+17+desplyact2);
 }
 
 void dibuja_hidden() {
  
  //Se dibuja en el panel de hidden layers
  //println(this.poscuad);
  
  int[] dest = getXYhidden(this.poscuad);
  destX = dest[0];
  destY = dest[1];
  
  if (this.selected) {
    destX = (mouseX);
    destY = (mouseY);     
   }
   
  if (!modoSeco) {
     float difX = actX - destX;
     float difY = actY - destY;
     if (Math.abs(difX) < despla) actX = destX;
       else actX = actX - difX/3;
   
     if (Math.abs(difY) < despla) actY = destY;
       else actY = actY - difY/3;
   } else {
     actX = destX;
     actY = destY;
     
   }   
   
   if (enTransicion) {
   dibuja_transiHidden();
   } else {
    
   if (actY+desplyact2 > bandaInf || this.isSelected()) {
     
   stroke(10); 
   fill(50,50,50);
   rect(actX+5,actY+desplyact2+5,anchoLab,altoLab);
   fill(254,242,222);
   rect(actX,actY+desplyact2,anchoLab,altoLab);
   rect(actX+anchoLab-70,actY+desplyact2,70,altoLab);
   textSize(15); fill(10);
   
   String cad="";
   
   if (listaHexSel.size()>0 && !this.nombre.equals("(Hits)")  && !this.nombre.equals("(Cluster)") ) {
     int sHits = (int) (sumaHits());
     if (sHits > 0 ) {
     float wMean = (int) (100 * weightMean()) ;
      wMean = wMean/100;
     float percent = 0;
     if (this.maxvalor!=this.minvalor)
      percent = (int) (10000*(wMean-this.minvalor)/(this.maxvalor-this.minvalor));
    
     percent = percent/100;
     cad = percent+" %";  
   
     } else cad = "0 hits"; 
 
   }
   String cadnombre = this.nombre.trim();
   while (textWidth(cadnombre+".") > anchoLab-75) cadnombre = cadnombre.substring(cadnombre.length()-1)+".";
   text(cadnombre,actX+5,actY+17+desplyact2);
   
   
   text(cad,actX+anchoLab-70+5,actY+17+desplyact2);
   
   }
  }
 }
 
 
 
 void dibuja_visible() {
   
   destX = constescala*(margenlado+margeninterno+(anchosinmarg*((poscuad-1)%numcolumnas)));
   destY = constescala*(margenalto+(margenaltint+tambloquey)*( (int) ((poscuad-1)/numcolumnas)));
   
   if (this.selected) {
    destX += constescala*(mouseX - xOffLayer);
    destY += constescala*(mouseY - yOffLayer);     
   }
   
   if (!modoSeco) {
     float difX = actX - destX;
     float difY = actY - destY;
     if (Math.abs(difX) < despla) actX = destX;
       else actX = actX - difX/3;
   
     if (Math.abs(difY) < despla) actY = destY;
       else actY = actY - difY/3;
   } else {
     actX = destX;
     actY = destY;     
   }   
   
   float posxTransla = actX;
   float posyTransla = actY;
   
   
   
   if (desplyact+posyTransla/constescala < height) { //Se pinta solo si cabe
   
     
   pushMatrix();
   
   //Segun las columnas acordadas, tenemos un ancho determinado.
   
   scale(ancho/anchograf,ancho/anchograf);
   
   translate(posxTransla,posyTransla);
    
   if (this.selected) {
       for (int i=0;i<this.filas*this.cols;i++)
         this.gridValores[i].display_gris();
       for (int i=0;i<this.filas*this.cols;i++)
         this.gridValores[i].display(-60,-60,255);  
   } else {
       for (int i=0;i<this.filas*this.cols;i++) {
          this.gridValores[i].display(255); 
       }
       int colred = color(0,0,0);
       for (int i=0;i<listaHexSel.size();i++) {
         
        this.gridValores[listaHexSel.get(i)[0]].display_dist(listaHexSel.get(i),colred);
       } 
   }
         
   
   popMatrix();
   
   float kini = (-rad);
   float kfin = (yfinpos-rad/2);
   int numk = (int) kfin - (int) kini;
   float diferen = sinRad;
   int posxLeyen = (int) ((posxTransla-diferen)/constescala) ;
   int posyLeyen = (int) ((posyTransla+kfin+15)/constescala);
   float tamtexto = (10*anchograf/ancho)/constescala; 

    stroke(20);
    strokeWeight(1);
    noFill();   
   if (muestra_cuad) {
      
   float anchoLeyen = 15; 
   
   image(rec_pseudo,posxLeyen,posyLeyen+5,finalpos,anchoLeyen);
   
   //Ahora se pinta el recuadro y las lineas de mayor, medio y menor

    rect(posxLeyen,posyLeyen+5,finalpos,anchoLeyen);
    fill(0);
    line(posxLeyen,posyLeyen+5,posxLeyen,posyLeyen+5+anchoLeyen+10);
    line(posxLeyen+finalpos,posyLeyen+5,posxLeyen+finalpos,posyLeyen+5+anchoLeyen+10);
    line(finalpos/2+posxLeyen,posyLeyen+anchoLeyen+5,finalpos/2+posxLeyen,posyLeyen+15+anchoLeyen);
     
    fill(40);
    
    textSize(tamtexto);
   
   float posyTexto = posyLeyen+10+anchoLeyen+2*tamtexto;
   
   if ((this.maxvalor == (int) this.maxvalor)&&(this.minvalor == (int) this.minvalor)) { 
     text((int)this.maxvalor, posxLeyen+finalpos-textWidth(trim(str((int)this.maxvalor))), posyTexto); 
     text((int)this.minvalor, posxLeyen, posyTexto ); 
     text(trim(str((int)(this.mediovalor))), finalpos/2+posxLeyen-(textWidth(trim(str((int)this.mediovalor))))/2, posyTexto); 
   } else {
     text(trim(str(this.maxvalor)), posxLeyen+finalpos-textWidth(trim(str(this.maxvalor))),  posyTexto); 
     text(trim(str(this.minvalor)), posxLeyen, posyTexto ); 
     text(trim(str(this.mediovalor)), finalpos/2+posxLeyen-(textWidth(trim(str(this.mediovalor))))/2, posyTexto); 
   }
  
   if (listaHexSel.size()>0 && !this.nombre.equals("(Hits)")) {
     textSize(tamtexto);
     fill(40);
     int sHits = (int) (sumaHits());
     if (sHits > 0 ) {
     float wMean = (int) (100 * weightMean()) ;
      wMean = wMean/100;
     float percent = 0;
     if (this.maxvalor!=this.minvalor)
      percent = (int) (10000*(wMean-this.minvalor)/(this.maxvalor-this.minvalor));
    
     percent = percent/100;
     
   
     text("W. Mean: "+wMean+" | "+percent+" % diff. | "+(int) (sHits)+" hits", posxLeyen, posyLeyen+10+20+3*tamtexto);
     } else text ("0 hits", posxLeyen, posyLeyen+10+20+3*tamtexto);
   
 }
   } else {
   //Si se esta seleccionando de subnet, que aparezca debajo el nombre de la red
   if (this.nombre.equals("(Subnet navigation)")&& (capaDentro != -1 && capaDentro == capaSubnet)) {
      
     float posyTexto = posyLeyen+10+2*tamtexto;
     textSize(tamtexto);
     fill(40);

     int i=0;
     int redSelec = -1;
     boolean enc = false;
     while (i<this.filas*this.cols && !enc) {
      if (this.gridValores[i].intoHex(mouseX,mouseY,actX,actY)) {
            enc = true;
            redSelec = mySOMs.subRedFromPos(actualSOM,xmlActual,i,this.filas,this.cols);
             println (redSelec);         
          };
      i++;  
     }   
     
     if (enc) text("Go to: "+somNames[redSelec], posxLeyen, posyTexto);
   }
 }
   
   fill(40);
   
   textSize((float) (tamtexto*1.7));
   text(this.nombre, posxLeyen,(posyTransla-rad-72)/constescala);
 }
 
 
 }
   
}

//********************** FIN CLASE LAYERSOM



//CLASE HEXAGONO *********************************************************************

   class Hexagon{
       float x,y,radi,colorfill,valor;
       float angle = (float) 360.0 / 6;
       int colorhex_pseudo;
       int[] coordx;
       int[] coordy;
       
     Hexagon(float cx, float cy, float r,float colfill,float valo)
    {
      x=cx;
      y=cy;
      valor=valo;
      radi=r;
      colorfill = colfill;
      
      colorhex_pseudo = calculaPseudocolor(colorfill/maxcolor);
      coordx = new int[6];
      coordy = new int[6];
      for (int i = 0; i < 6; i++)
      {
        coordx[i]=(int) (x + radi * sin(radians(angle * i)));
        coordy[i]=(int) (y + radi * cos(radians(angle * i)));
      }
    }

     boolean  intoHex(int xm, int ym,float actX,float actY) {
       boolean dentro = false;
       float proporcion = ancho/anchograf;
          
          if (xm-margenizda>=proporcion*(actX+x-sinRad) && xm-margenizda<=(actX+x+sinRad)*proporcion) {
             if (ym-desplyact>=proporcion*(actY+y-sinRad) && ym-desplyact<=(actY+y+sinRad)*proporcion) {
               dentro = true;
               
            }
         }
       return dentro;
     }
     
     
     boolean  coincideColor(float actX,float actY) { //true si coincide en pantalla el mismo color que tiene designado
      
         float proporcion = ancho/anchograf;
         loadPixels();
         return (get((int) (proporcion*(actX+x)+margenizda), (int) (proporcion*(actY+y)+desplyact)) == colorhex_pseudo);
          
     }
     
     
     
     
     void display_gris() {
     
     fill(100);
        
     //stroke(255);
     strokeWeight((float) 1);
     beginShape();
      for (int i = 0; i < 6; i++)
              vertex(coordx[i],coordy[i]);
      endShape(CLOSE);      
    }
     
     void display(int strok) {
      display(0,0,strok);
     }
     
     void display_dist(Integer[] dibujo, int strok){
  
     stroke(strok);
     strokeWeight((float) 20);
     
       for (int i = 1; i <= 6; i++) {
        if (dibujo[i]==1) {
        
             int isig = i; if (isig==6) isig=0;
              line(coordx[i-1],coordy[i-1],coordx[isig],coordy[isig]);
         }     
       }       
      
    }
    
     void display(int xOff, int yOff, int strok){
     float stWe = (float) 3.5;
     
     if (strok==0) stWe = 20;
     fill(colorhex_pseudo);
        
     stroke(strok);
     strokeWeight((float) stWe);
     beginShape();
      for (int i = 0; i < 6; i++)
              vertex(coordx[i]+xOff,coordy[i]+yOff);
      
      endShape(CLOSE);      
    }
    }

//FIN CLASE HEXAGONO

//***************** FUNCIONES GUI

public void Cancel() { 
  if (modoCalculando) {
   cancelado = true; 
   experimento.sendCancel();
   File fil = new File(directorio);
   deleteFile(fil);
   println("Borrando "+directorio);
  }
}

public void handleKnobEvents(GValueControl knob, GEvent event) { /* code */ }

public void handleToggleControlEvents(GToggleControl option, GEvent event) {
  
  if (option == cbxBatch)
    propi.useBatch(option.isSelected());
    
  if (option == cbxOnline)
   propi.useOnline(option.isSelected());
  
  if (option == cbxSetGrow) {
   propi.setGrowing(option.isSelected());
  }
  
  if (option == cbxSetHier) {
    propi.setHier(option.isSelected());
  }
  
  if (option == cbxSetGCHSOM) {
  propi.setGCHSOM(option.isSelected());
  }
  
  if (option == cbxSetSize) {
    propi.setSizeAut(option.isSelected());    
    if (!option.isSelected()) {        
       propi.setXYSOM(slParamSom.getValueXI(),slParamSom.getValueYI());
      }
     /*lbwidthSom.setVisible(!option.isSelected());
     lbheightSom.setVisible(!option.isSelected());*/
     slParamSom.setVisible(!option.isSelected());
    }
  
  if (option == cbxPCA) propi.setInitPCA(option.isSelected());
  if (option == cbxRandom) propi.setInitRandom(option.isSelected());
  if (option == cbxInter) propi.setInitInterval(option.isSelected());
  if (option == cbxVector) propi.setInitVector(option.isSelected());
  
  if (option == cbxGauss) propi.setNeighGauss(option.isSelected()); 
  if (option == cbxMH) propi.setNeighMH(option.isSelected());
  if (option == cbxCutGauss) propi.setNeighCutGauss(option.isSelected());
  if (option == cbxBobble) propi.setNeighBobble(option.isSelected());
  
  if (option == chSelection) selPropi.setEnableCell(option.isSelected());
   
  if (option == OCustom) selPropi.setSelCustom();
  if (option == OCluster) {
      selPropi.setSelCluster();
      actualizaClusters();
  }
  if (option == ORadius) selPropi.setSelRadius();
  
  
  
}


public void handleButtonEvents(GButton button, GEvent event) { 
  // Folder selection
  
  if (button == btnInput) {
  
    String[] datosFich = G4P.selectInputDif("Seleccione fichero", "CSV", "Ficheros CSV");
    String fname = datosFich[0];
    if (fname!=null && !modoCalculando) {
    if (trim(propi.getExpName()).equals("")) propi.setExpName(datosFich[1]);
    //lbCurFile.setText(fname, GAlign.LEFT, GAlign.MIDDLE);
    propi.setFicheroEntrada(fname);
    menu = false;
    tipo = this.FILE;
    thread("EnviaEntrenador");
    }
  };  
    
  if (button == btnCluster) {
     clusteriza();  
     if (selPropi.getStrSel().equals("SelCluster")) actualizaClusters();     
  }   
  
  if (button == btnPDF) {
     fPDFOutput = G4P.selectOutput("PDF output");
     if (!fPDFOutput.equals("")) {
      PDFoutput=true;
     }
     //lblFile.setText(fname);
  }
  
 if (button == btnSelected) {
  boolean correcto = true;
  
 if (listaHexSel.size()==0) {
   G4P.showMessage(this, "You must select cells first (dbl click)", "Selected cells required", G4P.INFO);
   correcto = false;
 } 
 
 if (trim(propi.getExpName()).equals("")) {
     G4P.showMessage(this, "You must give a title", "Title required", G4P.INFO);
     correcto = false;
   } 
 
 if (correcto) {
    menu = false;
    tipo = this.SELEC;
    
    thread("EnviaEntrenador"); 
 }
}

  
  if (button == btnCloseMenu) {
   menu = false;
   actualizaMenus();
  }
    
  if (button == btnIteration) wIteration.setVisible(!wIteration.isVisible());
  
  if (button == btCloseWIteration) wIteration.setVisible(false);
  
  if (button == btDelete) {
    int reply = G4P.selectOption(this, "Delete current net?", "Delete",  G4P.QUERY, G4P.YES_NO);
    if (reply== G4P.OK) {
      File fil = new File(actualFolder);
      deleteFile(fil);
      inicia_carpetas();
      if (mySOMs.size()>0) {
      calcPosMenus();     
      actualFolder = somFolders[0];
      actualSOM = 0;
      carga_expprops();
      carga_colormaps();
      carga_selprops();
      carga_som();
      }
      
    }
  }
   
}

public void EnviaEntrenador () {
 
  try  {    
   
    
   modoCalculando = true;
   menu = false;
   actualizaMenus();
   
   inicioCalc = millis();  
   Calendar cal = Calendar.getInstance();
   int year = cal.get(Calendar.YEAR);
   int month = cal.get(Calendar.MONTH);
   int day = cal.get(Calendar.DAY_OF_MONTH);
   int hour = cal.get(Calendar.HOUR_OF_DAY);
   int minute = cal.get(Calendar.MINUTE);
   int second = cal.get(Calendar.SECOND);
   
   
   if (tipo == this.FILE) {
     directorio = dataPath("nets/n"+year+month+day+hour+minute+second);
     propi.setIsSubred(false);
     } else {
     if (datos1 == null) datos1 = new LFSData(dataPath(actualFolder+"/data.csv"));
     gsom = new LFSGrowingSOM(dataPath(actualFolder+"/"+xmlActual),datos1);
     directorio = actualFolder+"/n"+year+month+day+hour+minute+second;     
     gsom.saveMapCSVParcial(datos1,HexDist.getIncluidos(),directorio,directorio+"/data.csv");
     propi.setFicheroEntrada(directorio+"/data.csv");
     //actualFolder = directorio;
     propi.setIsSubred(true);
     propi.setSubredOrigen(HexDist.getIncluidos());
     propi.setFPadre(xmlActual);
    }
   
   
   propi.setDataPath(directorio);
   propi.setRootPath(dataPath(""));
   
    boolean esta = true;
    while (esta) {
      esta = false;
      for (int w=0;w<somNames.length;w++)
       if (propi.getExpName().equals(somNames[w])) {
          propi.setExpName(propi.getExpName()+"-"); esta = true;
       }   
    }     
   
   propi.setSensiCluster(gtSensiCluster.getText());
   propi.setCycles(gtCycles.getText());
   propi.setLambda(gtLambda.getText());
   
   propi.setSigma(gtSigma.getText());
   propi.setTau(gtTau.getText());
   propi.setTau2(gtTau2.getText());
      
   propi.setSizeAut(!slParamSom.isVisible());
      
   propi.setExpName(gtTitle.getText());
   propi.setGCHSOM(cbxSetGCHSOM.isSelected());
   propi.setGrowing(cbxSetGrow.isSelected());
   propi.setHier(cbxSetHier.isSelected());
   propi.useBatch(cbxBatch.isSelected());
   propi.useOnline(cbxOnline.isSelected());
 
   propi.setInitPCA(cbxPCA.isSelected());
   propi.setInitRandom(cbxRandom.isSelected());
   propi.setInitInterval(cbxInter.isSelected());
   propi.setInitVector(cbxVector.isSelected());
   propi.setNeighCutGauss(cbxCutGauss.isSelected());
   propi.setNeighGauss(cbxGauss.isSelected());
   propi.setNeighMH(cbxMH.isSelected());
   propi.setNeighBobble(cbxBobble.isSelected());
   propi.setNumCPUs(gtCPU.getText());
 
   int somMuestra =  mySOMs.size();
   experimento.LanzaExperimento(propi); 
    while (!cancelado && experimento.getProgreso()<100)
     {}
   kb.setVisible(false);
   bCancel.setVisible(false);
  
   inicia_carpetas();
   if (!cancelado) {
     carga_som_selec(somMuestra);
   }
   cancelado = false;
   
   experimento = new TrainSelector(); 
   modoCalculando = false;
   
}  catch (Exception e) {
    println ("Exception: "+e); 
};
}


public void handleSliderEvents(GValueControl slider, GEvent event){
  if (slider==sdnClusters) nCluster = slider.getValueI();
  if (slider==sdnColumn) { 
     desplyact = 0;
     numcolumnas = slider.getValueI();
  }
  if (slider == snRadius)
    actualizaRadius();
}

public void handleSlider2DEvents(GSlider2D slider2d, GEvent event) {
    
    propi.setWidthSOM(slParamSom.getValueXI());
    propi.setHeightSOM(slParamSom.getValueYI());

    
}

public void handleDropListEvents(GDropList list, GEvent event) { /* code */ 
 if (list==dErrIndex) xmlActual = indexfiles[list.getSelectedIndex()];
    
 
 if (list==dColorMap) cMapAct = (int)list.getSelectedIndex();
 
 modoSeco = true;
 actualiza_color();
 
}




public void handlePanelEvents(GPanel panel, GEvent event) { 
 
 if (event.toString().equals("Control was expanded")) {
   if (panel==pVisualization) {
     pnlMySOM.setCollapsed(true);
     pSelection.setCollapsed(true);
     pnlTrain.setCollapsed(true);
   } 
   
   if (panel==pSelection) {
     pnlMySOM.setCollapsed(true);
     pVisualization.setCollapsed(true);
     pnlTrain.setCollapsed(true);
   } 
   
   if (panel==pnlTrain) {
     pnlMySOM.setCollapsed(true);
     pVisualization.setCollapsed(true);
     pSelection.setCollapsed(true);
   } 
   
   if (panel==pnlMySOM) {
     pVisualization.setCollapsed(true);
     pSelection.setCollapsed(true);
     pnlTrain.setCollapsed(true);
   }
  

   
  
 }
 
 if (menu) calcPosMenus();
  else listSOM.setVisible(false);
 
 
 calcMargenIzda();
}


public void handleTextEvents(GEditableTextControl tc, GEvent event) { 
  //System.out.print("\n" + tc.tag + "   Event type: ");
  if (tc == gtIterations && Integer.parseInt(gtIterations.getText())!=propi.getNumRepe()) propi.setNumRepe(Integer.parseInt(gtIterations.getText()));
  switch(event) {
  case CHANGED:
   if (tc == gtLearning) propi.setBucleLearnRate(gtLearning.getText());
   if (tc == gtSigma) propi.setSigma(gtSigma.getText());
   if (tc == gtNWidth) propi.setBuclePcNeighWidth(gtNWidth.getText());
   if (tc == gtTitle) {
      boolean esta = true;
      while (esta) {
        esta = false;
        for (int w=0;w<somNames.length;w++)
         if (gtTitle.getText().equals(somNames[w])) {
            gtTitle.setText(gtTitle.getText()+"-"); esta = true;
         }   
      }     
      
      propi.setExpName(gtTitle.getText());
   }   
   if (tc == gtCPU) propi.setNumCPUs(gtCPU.getText());
    //System.out.println("CHANGED");
    break;
  case SELECTION_CHANGED:
    //System.out.println("SELECTION_CHANGED");
    //System.out.println(tc.getSelectedText() + "\n");
    break;
  case ENTERED:
    //System.out.println("ENTER KEY TYPED");
    //System.out.println(tc.getSelectedText() + "\n");
    break;
  default:
    //System.out.println(event);
  }
}





public void dibuja_hidden_panel() {
 int alturaPanel = 240;
 
 stroke(25);
 fill(100,100,100,210);
 rect(5,height-alturaPanel,width-15,height,15,15,0,0);
 //line(0,height-alturaPanel+20,width,height-alturaPanel+20);
 if (nOcultos==0) {
 fill(250);textSize(20);
 text("Hidden layers (drag here to hide)",25,bandaInf+30);
 }
}


public void customize_kb() {
  

  PApplet.useNativeSelect = true;
 

 //knob  ********************************************************************************
  kb = new GKnob(this, width/2-90, height/2-90, 180, 180, 0.8f);
  kb.setLimits(0,100);
  kb.setValue(0);
  kb.setVisible(false);
  
  bCancel = cp5.addButton("Cancel")
     .setValue(0)
     .setPosition(widthreal/2-17,height/2-10)
     .setSize(35,19)
     ;
     
     
  bCancel.setVisible(false);   
  
  
  btnCloseMenu = new GButton(this, 150, bandaInf-50, 80, 30);
  btnCloseMenu.setText("Close");
  btnCloseMenu.setLocalColorScheme(8);
  btnCloseMenu.setVisible(false);

}

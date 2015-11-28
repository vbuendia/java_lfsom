
public void quita_listSOM () {
  
  listSOM.remove();  
  cp5.remove("Select SOM");
}



public void controlEvent(ControlEvent theEvent) {
 // println(theEvent.controller().name()+" = "+theEvent.value()); 
 if  (theEvent.controller().name().equals("Select SOM")) {
  actualSOM = (int) Math.floor(theEvent.value());
  actualFolder = somFolders[actualSOM];
  
  desplyact = 0;
  carga_expprops();
  carga_selprops();
  dErrIndex.setItems(indexes,0);
  carga_som();

 }


}


public void inserta_som(MultiListButton lbi, int pos, int cnt) {
  
 ArrayList<structNet> redesHijos = mySOMs.subredesFrom(pos);
 
 for (int i = 0; i<redesHijos.size();i++) {
     String nameNet = redesHijos.get(i).getNetName();
     MultiListButton c = lbi.add("nivel"+redesHijos.get(i).getIndice(),redesHijos.get(i).getIndice());
     c.setId(cnt);
     c.setLabel(nameNet);
     c.setColorBackground(color(64 + 18*i,0,0));
     inserta_som(c,redesHijos.get(i).getIndice(),++cnt);
 }
  

}



public void update_soms(int posx, int posy) {
  

  listSOM = cp5.addMultiList("Select SOM",posx,posy,100,12);
    
    // create a multiListButton which we will use to
    // add new buttons to the multilist
    MultiListButton b;
   
    int cnt = 100;
    ArrayList<structNet> redesIniciales = mySOMs.redesIniciales();
    for (int i = 0;i<redesIniciales.size();i++) {
       String nameNet = redesIniciales.get(i).getNetName();
       b = listSOM.add(nameNet,redesIniciales.get(i).getIndice());
       b.setLabel(nameNet);
       b.setWidth(205);
       b.setHeight(20);
       b.setColorBackground(color(64 + 18*i,0,0));
         //lbi.add("pru"+i,i+200);
        inserta_som(b,redesIniciales.get(i).getIndice(),++cnt);
    }


}



public void customize_gui_mysoms() {
  int AnchoPanel = 230;
  
  pnlMySOM = new GPanel(this, 0,0, AnchoPanel,tamMenuSOMs  , " My SOMs");
  pnlMySOM.setLocalColorScheme(8);
  pnlMySOM.setDraggable(false);
  int[] alturas  =   {35,70,100,135,180,195,230,300};
  GLabel lbTrainPar = new GLabel(this, 0, 0, 190, 20);
  lbTrainPar.setText("Navigate your SOMs",GAlign.LEFT, GAlign.LEFT);
  lbTrainPar.setTextBold();
  pnlMySOM.addControl(lbTrainPar,10,alturas[0]);
  
   listSOM = cp5.addMultiList("Select SOM",100,tamMenuSOMs-120,200,300)
         .setPosition(100, 300)
         .setSize(200, 300)
         .setColorBackground(color(255, 128))
         .setColorActive(color(0))
         .setColorForeground(color(210, 210,150))
         .setVisible(false);
         ;
  //update_soms(100,100);              
  
  btDelete = new GButton(this, 210, 410, 160, 30);
  btDelete.setText("Delete current net");
  pnlMySOM.addControl(btDelete,10,tamMenuSOMs-50);
  
  pnlMySOM.setCollapsed(true);
  pnlMySOM.setVisible(false);
}



public void customize_gui_train() {

  int AnchoPanel = 230;
  
  pnlTrain = new GPanel(this, 0,0, AnchoPanel,tamMenuTrain  , " Generate new SOM");
  pnlTrain.setLocalColorScheme(7);
  pnlTrain.setDraggable(false);
  pnlTrain.setCollapsed(false);  

  int[] alturas  =   {35,70,90,195,230,245,280,350};
  int altactual = 0;
  
  int posxtrain = 10;
  GLabel lbTrainPar = new GLabel(this, 0, 0, 190, 20);
  lbTrainPar.setText("New SOM title",GAlign.LEFT, GAlign.LEFT);
  lbTrainPar.setTextBold();
  pnlTrain.addControl(lbTrainPar,posxtrain,alturas[altactual]);
  
  gtTitle = new GTextField(this, 170, 360, 100, 20, G4P.SCROLLBARS_NONE);
  gtTitle.setOpaque(true);
  pnlTrain.addControl(gtTitle,posxtrain+85,alturas[altactual]);
  
  altactual++;
  
  slParamSom = new GSlider2D(this, 0, 0, 133, 30);
  slParamSom.setLimitsX(2, 20);
  slParamSom.setLimitsY(2, 20);
  slParamSom.setEasing(4);
  slParamSom.setValueXY(propi.getWidthSOM(), propi.getHeightSOM());
  pnlTrain.addControl(slParamSom,posxtrain+85,alturas[altactual]+5);
  slParamSom.setVisible(!propi.isSizeAut());
  
  altactual++;
  
  cbxSetSize = new GCheckbox(this, 0, 0, 160, 18, "Auto Size");  
  pnlTrain.addControl(cbxSetSize, posxtrain, alturas[altactual]-5);
  cbxSetSize.setSelected(propi.isSizeAut());

  cbxSetGrow = new GCheckbox(this, 0, 0, 160, 18, "Growing");  
  pnlTrain.addControl(cbxSetGrow, posxtrain, alturas[altactual]+20);
  cbxSetGrow.setSelected(propi.isGrowing());

  cbxSetHier = new GCheckbox(this, 0, 0, 160, 18, "GHSOM");  
  pnlTrain.addControl(cbxSetHier, posxtrain, alturas[altactual]+45);
  cbxSetGrow.setSelected(propi.isHier());

  cbxSetGCHSOM = new GCheckbox(this, 0, 0, 160, 18, "GCHSOM");  
  pnlTrain.addControl(cbxSetGCHSOM, posxtrain, alturas[altactual]+70);
  cbxSetGCHSOM.setSelected(propi.isGCHSOM());


  altactual++;
  
  btnIteration = new GButton(this, 0, 0, 140, 20, "Config train iteration"); 
  pnlTrain.addControl(btnIteration,posxtrain,alturas[altactual]);

  altactual++;
  
  GLabel lbNCPU = new GLabel(this, 0, 0, 190, 20);
  lbNCPU.setText("N. of CPUs:", GAlign.LEFT, GAlign.LEFT);
  pnlTrain.addControl(lbNCPU,posxtrain, alturas[altactual]);
    
  gtCPU = new GTextField(this, 10, 13, 25, 17);
  gtCPU.setText(propi.getStrNumCPUs());
  pnlTrain.addControl(gtCPU,posxtrain+75,alturas[altactual]);

   
  GLabel lbCurCsv = new GLabel(this, 0, 0, 140, 60);
  lbCurCsv.setText("Create new SOM from:", GAlign.LEFT, GAlign.LEFT);
  lbCurCsv.setTextBold();
  //lbCurFile = new GLabel(this, 0, 0, 180,60);
  //lbCurFile.setText(propi.getFicheroEntrada(), GAlign.LEFT, GAlign.LEFT);
  btnInput = new GButton(this, 0, 0, 140, 20, "Train from CSV file"); 
  btnSelected = new GButton(this, 0, 0, 140, 20, "Train from selection"); 
  
  altactual++;
  
  pnlTrain.addControl(lbCurCsv,posxtrain, alturas[altactual]);
  
  altactual++;
  
  //pnlTrain.addControl(lbCurFile,posxtrain,alturas[6]);
  pnlTrain.addControl(btnInput,posxtrain,alturas[altactual]+10);
  pnlTrain.addControl(btnSelected,posxtrain,alturas[altactual]+40);
  //btnSelected.setEnabled(false);
  
  pnlTrain.setCollapsed(true);
  pnlTrain.setVisible(false);
  
}


public void createGUIPSelection(){
  //int posPanel = width/2-width/3;
  //int alturaPanel = 240; //el panel inferior
  
  pSelection = new GPanel(this, 150, bandaInf-10, 230, tamMenuSel, "   Selection");
  pSelection.setText("   Selection");
  pSelection.setOpaque(true);
  pSelection.setDraggable(false);  
  pSelection.setCollapsible(true);  
  pSelection.setVisible(true);
  pSelection.setEnabled(true);
  //pnl.addControl(pSelection,posPanel,30);
  pSelection.setLocalColorScheme(8);
  
  chSelection = new GCheckbox(this, 5, 30, 220, 40);
  chSelection.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  chSelection.setText("Enable cell selection (dbl click to freeze/clear)");
  chSelection.setTextBold();
  chSelection.setOpaque(false);
  chSelection.setSelected(selPropi.getEnableCell());
  pSelection.addControl(chSelection);
  
  togGroup1 = new GToggleGroup();
  ORadius = new GOption(this, 5, 100, 70, 22);
  ORadius.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  ORadius.setText(" Radius");
  ORadius.setOpaque(false);
  
  OCluster = new GOption(this, 5, 130, 120, 20);
  OCluster.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  OCluster.setText(" Cluster");
  OCluster.setOpaque(false);
  
  OCustom = new GOption(this, 5, 160, 220, 20);
  OCustom.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  OCustom.setText(" Custom (dbl click start/finish)");
  OCustom.setOpaque(false);
  
  /*
  OSubnet = new GOption(this, 5, 190, 220, 20);
  OSubnet.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  OSubnet.setText(" Subnets (left and right click to navigate)");
  OSubnet.setOpaque(false);
  */
  
  
  togGroup1.addControl(ORadius);
  ORadius.setSelected(selPropi.isSelRadius());
  OCustom.setSelected(selPropi.isSelCustom());
  OCluster.setSelected(selPropi.isSelCluster());
  pSelection.addControl(ORadius);
  togGroup1.addControl(OCluster);
  pSelection.addControl(OCluster);
  togGroup1.addControl(OCustom);
  pSelection.addControl(OCustom);
  
  
   
  GLabel label1 = new GLabel(this, 5, 70, 140, 20);
  label1.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  label1.setText("Selection mode");
  label1.setTextBold();
  label1.setTextItalic();
  label1.setOpaque(false);
  
  snRadius = new GCustomSlider(this, 100, 75, 100, 70, "grey_blue");
  snRadius.setShowDecor(false, true, true, true);
  snRadius.setNumberFormat(G4P.EXPONENT, 1);
  snRadius.setLimits(0,10);
  snRadius.setValue(radiusSel);
  snRadius.setOpaque(false);

  pSelection.addControl(chSelection);
  pSelection.addControl(label1);
  pSelection.addControl(snRadius);
  
  pSelection.setVisible(false);
 
}


public void createGUIPVisualization(){
  
  int posPanel = (int) ( width/2-width/3+ width*0.6/3+10);
  int alturaPanel = 240; //el panel inferior
  
  pVisualization = new GPanel(this, 450, bandaInf-10, 230, tamMenuVis, "   Visualization");
  pVisualization.setText("   Visualization");
  pVisualization.setOpaque(true);
  pVisualization.setDraggable(false);  
  pVisualization.setCollapsible(true);  
  //pVisualization.setEnabled(false);
  //pnl.addControl(pVisualization,posPanel,30);
  pVisualization.setLocalColorScheme(4);
  GLabel glColorMap = new GLabel(this, 10, 40, 105, 20);
  glColorMap.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  glColorMap.setText("Select Color Map");
  glColorMap.setOpaque(false);
  
  GLabel label2 = new GLabel(this, 10, 100, 105, 20);
  label2.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  label2.setText("Layers per row");
  label2.setOpaque(false);
  
  GLabel lbselect = new GLabel(this, 10, 160, 132, 20);
  lbselect.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lbselect.setText("Select SOM");
  lbselect.setOpaque(false);
  
  
  dErrIndex = new  GDropList(this, 100, 163, 115, 66, 3);
  dErrIndex.setItems(indexes,0);
  dErrIndex.setOpaque(true);
  //xmlActual=indexes[0];
  dErrIndex.setVisible(true);  
  
  dColorMap = new  GDropList(this, 115, 43, 105, 120, 6);
  dColorMap.setItems(colorMapNames,0);
  dColorMap.setVisible(true);
     
  
  GLabel label1 = new GLabel(this, 10, 220, 110, 20);
  label1.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  label1.setText("Clusterize SOM in");
  label1.setOpaque(false);
  
  sdnColumn = new GCustomSlider(this, 120, 70, 80, 80, null);
  // show          opaque  ticks value limits
  sdnColumn.setShowDecor(false, true, true, true);
  sdnColumn.setNumberFormat(G4P.EXPONENT, 1);
  sdnColumn.setLimits(3,6);
  
  sdnClusters = new GCustomSlider(this,120, 190, 80, 80, null);
  // show          opaque  ticks value limits
  sdnClusters.setShowDecor(false, true, true, true);
  sdnClusters.setNumberFormat(G4P.EXPONENT, 1);
  sdnClusters.setLimits(4,15);
  nCluster = sdnClusters.getValueI();
  
  btnCluster = new GButton(this, 120, 260, 80, 30);
  btnCluster.setText("Clusterize");
   
  btnPDF = new GButton(this,10,315,80,20);
  btnPDF.setText("PDF Export");
   
   
   cp5 = new ControlP5(this);
       
  
  pVisualization.addControl(glColorMap);
  pVisualization.addControl(label2);
  pVisualization.addControl(lbselect);
  pVisualization.addControl(label1);
  pVisualization.addControl(sdnColumn);
  pVisualization.addControl(sdnClusters);
  pVisualization.addControl(btnCluster);
  pVisualization.addControl(dErrIndex);
  pVisualization.addControl(dColorMap);
  pVisualization.addControl(btnPDF);
  dErrIndex.setLocalColorScheme(7);
  dColorMap.setLocalColorScheme(6);
  
  pVisualization.setCollapsed(true);
  pVisualization.setVisible(false);
}




public void createGUIWIteration(){
  
  int desp = 20;
  wIteration = new GPanel(this, 250, 100, 310, 520 , "Train Bucle Config");
  wIteration.setLocalColorScheme(7);
  wIteration.setVisible(false);
  wIteration.setCollapsible(false);
  
  GLabel lbIterations = new GLabel(this, 20, 20, 138, 20);
  lbIterations.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lbIterations.setText("Number of iterations:");
  lbIterations.setTextBold();
  lbIterations.setTextItalic();  
  wIteration.addControl(lbIterations,20,20+desp);
  
  gtIterations = new GTextField(this, 170, 20, 100, 20, G4P.SCROLLBARS_NONE);
  wIteration.addControl(gtIterations,170,20+desp);
  
  GLabel lbsetinits = new GLabel(this, 20, 50, 80, 20);
  lbsetinits.setText("Initializations");
  lbsetinits.setTextBold();
  lbsetinits.setTextItalic();
  wIteration.addControl(lbsetinits,20,50+desp);
  
  cbxInter = new GCheckbox(this, 170, 110, 150, 20);
  cbxInter.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxInter.setText("Interval interpolate");
  wIteration.addControl(cbxInter,170,110+desp);
 
  cbxVector = new GCheckbox(this, 20, 110, 120, 20);
  cbxVector.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxVector.setText("Vector");
  wIteration.addControl(cbxVector,20,110+desp);
 
  cbxRandom = new GCheckbox(this, 170, 80, 120, 20);
  cbxRandom.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxRandom.setText("Random");
  wIteration.addControl(cbxRandom,170,80+desp);
 
  cbxPCA = new GCheckbox(this, 20, 80, 120, 20);
  cbxPCA.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxPCA.setText("PCA");
  wIteration.addControl(cbxPCA,20,80+desp);
 
  GLabel lbsetneigh = new GLabel(this, 20, 150, 150, 20);
  lbsetneigh.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lbsetneigh.setText("Neighbor Functions");
  lbsetneigh.setTextBold();
  lbsetneigh.setTextItalic();
  wIteration.addControl(lbsetneigh,20,150+desp);
 
  cbxCutGauss = new GCheckbox(this, 170, 180, 110, 20);
  cbxCutGauss.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxCutGauss.setText("Cut Gauss");
  wIteration.addControl(cbxCutGauss,170,180+desp);
 
  cbxBobble = new GCheckbox(this, 20, 210, 120, 20);
  cbxBobble.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxBobble.setText("Bubble");
  wIteration.addControl(cbxBobble,20,210+desp);
 
  cbxGauss = new GCheckbox(this, 20, 180, 120, 20);
  cbxGauss.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxGauss.setText("Gauss");
  wIteration.addControl(cbxGauss,20,180+desp);
 
  GLabel lbbatch = new GLabel(this, 20, 250, 150, 20);
  lbbatch.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lbbatch.setText("Training Modes");
  lbbatch.setTextBold();
  wIteration.addControl(lbbatch,20,250+desp);
 
  cbxOnline = new GCheckbox(this, 20, 280, 120, 20);
  cbxOnline.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxOnline.setText("On line");
  wIteration.addControl(cbxOnline,20,280+desp);
 
  cbxBatch = new GCheckbox(this, 170, 280, 120, 20);
  cbxBatch.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  cbxBatch.setText("Batch");
  wIteration.addControl(cbxBatch,170,280+desp);
 
  GLabel lblearning = new GLabel(this, 20, 330, 80, 20);
  lblearning.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lblearning.setText("Learn Rates");
  lblearning.setTextBold();
  lblearning.setTextItalic();
  wIteration.addControl(lblearning,20,330+desp);
 
  gtLearning = new GTextField(this, 20, 360, 130, 20, G4P.SCROLLBARS_NONE);
  gtLearning.setLocalColorScheme(7);
  gtLearning.setOpaque(true);
  wIteration.addControl(gtLearning,20,360+desp);
  
  
  GLabel lbsetsigma = new GLabel(this, 170, 330, 120, 20);
  lbsetsigma.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lbsetsigma.setText("Sigma Values");
  lbsetsigma.setTextBold();
  lbsetsigma.setTextItalic();
  wIteration.addControl(lbsetsigma,170,330+desp);
  
  gtSigma = new GTextField(this, 170, 360, 120, 20, G4P.SCROLLBARS_NONE);
  gtSigma.setOpaque(true);
  wIteration.addControl(gtSigma,170,360+desp);
  
  GLabel lbsetnwidth = new GLabel(this, 20, 310, 120, 20);
  lbsetnwidth.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lbsetnwidth.setText("Neighbour width");
  lbsetnwidth.setTextBold();
  lbsetnwidth.setTextItalic();
  wIteration.addControl(lbsetnwidth,20,390+desp);
  
  gtNWidth = new GTextField(this, 20, 360, 120, 20, G4P.SCROLLBARS_NONE);
  gtNWidth.setOpaque(true);
  wIteration.addControl(gtNWidth,20,420+desp);
  
  
  
  btCloseWIteration = new GButton(this, 210, 450, 80, 30);
  btCloseWIteration.setText("Close");
  btCloseWIteration.setLocalColorScheme(7);
  wIteration.addControl(btCloseWIteration,210,450+desp);
  
  gtIterations.setDefaultText(String.valueOf(propi.getNumRepe()));
  cbxPCA.setSelected(propi.getInitPCA());
  cbxRandom.setSelected(propi.getInitRandom());
  cbxInter.setSelected(propi.getInitInterval());
  cbxVector.setSelected(propi.getInitVector());
  cbxGauss.setSelected(propi.getNeighGauss());
  cbxCutGauss.setSelected(propi.getNeighCutGauss());
  cbxBobble.setSelected(propi.getNeighBobble());
  cbxOnline.setSelected(propi.getUseOnline());
  cbxBatch.setSelected(propi.getUseBatch());
  gtLearning.setText(propi.getStrBucleLearnRate());
  gtSigma.setText(propi.getStrBucleSigma());
  gtNWidth.setText(propi.getStrBuclePcNeighWidth());

  
  
}




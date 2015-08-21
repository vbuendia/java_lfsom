var maxSDHAnzahl;
var maxTWAnzahl;
var maxTPAnzahl;

function showClassInfo(elem, number, event) {
    setClassInfoText(number + ' elements on unit');
    showClassInfoDiv();
    positionClassInfoDiv(event);
}

function setClassInfoText(text) {
    var classInfoDiv = document.getElementById('classInfoDiv');
    while (classInfoDiv.lastChild != null) {
        classInfoDiv.removeChild(classInfoDiv.lastChild);
    }
    classInfoDiv.appendChild(document.createTextNode(text));
    var w = Math.ceil(text.length * 0.6);
    classInfoDiv.style.width = w + "em";
}

function showClassInfoDiv() {
    var classInfoDiv = document.getElementById('classInfoDiv');
    classInfoDiv.style.display = 'block';
}

function hideClassInfoDiv() {
    var classInfoDiv = document.getElementById('classInfoDiv');
    classInfoDiv.style.display = 'none';
}

function positionClassInfoDiv(event) {
    var classInfoDiv = document.getElementById('classInfoDiv');
    var values = getPositionForMouseObject(event);
    classInfoDiv.style.top = values[1];
    classInfoDiv.style.left = values[0];
}

// JS code for the mouse over information of the mapped input vectors
function showInputInfo(elem, qe, mqe, dist, unit, event) {
    setInputInfoText(qe, mqe, dist, unit);
    showInputInfoDiv();
    positionInputInfoDiv(event);
}

function setInputInfoText(qe, mqe, dist, unit) {
    var inputInfoDiv = document.getElementById('inputInfoDiv');
    var inputfield;
    inputfield = document.getElementById('inputInfoDiv_qe');
    while (inputfield.lastChild != null) {
        inputfield.removeChild(inputfield.lastChild);
    }
    inputfield.appendChild(document.createTextNode(qe));
    inputfield = document.getElementById('inputInfoDiv_mqe');
    while (inputfield.lastChild != null) {
        inputfield.removeChild(inputfield.lastChild);
    }
    inputfield.appendChild(document.createTextNode(mqe));
    inputfield = document.getElementById('inputInfoDiv_dist');
    while (inputfield.lastChild != null) {
        inputfield.removeChild(inputfield.lastChild);
    }
    inputfield.appendChild(document.createTextNode(dist));
    inputfield = document.getElementById('inputInfoDiv_unit');
    while (inputfield.lastChild != null) {
        inputfield.removeChild(inputfield.lastChild);
    }
    inputfield.appendChild(document.createTextNode(unit));
}

function showInputInfoDiv() {
    var inputInfoDiv = document.getElementById('inputInfoDiv');
    inputInfoDiv.style.display = 'block';
}

function hideInputInfoDiv() {
    var inputInfoDiv = document.getElementById('inputInfoDiv');
    inputInfoDiv.style.display = 'none';
}

function positionInputInfoDiv(event) {
    var inputInfoDiv = document.getElementById('inputInfoDiv');
    var values = getPositionForMouseObject(event);
    inputInfoDiv.style.top = values[1];
    inputInfoDiv.style.left = values[0];
}

// helper function
function getPositionForMouseObject(event) {
    if (!event) {
        event = window.event;
    }
    var yoffset = 0;
    if (window.pageYOffset > 0) {
        yoffset = window.pageYOffset;
    } else {
        yoffset = document.documentElement.scrollTop;
    }
    var xoffset = 0;
    if (window.pageXOffset > 0) {
        xoffset = window.pageXOffset;
    } else {
        xoffset = document.documentElement.scrollLeft;
    }
    var x = event.clientX + 20 + xoffset + 'px';
    if (this.status == 2) {
        x = event.clientX - 20 - 550 - xoffset + 'px';
    }
    return new Array(x, event.clientY + yoffset + 'px');
}

// JS function for expanding and collapsing the cluster tree
function swapClusterDisp(level) {
    var content = document.getElementById("clusterNode_" + level);
    if (content.style.display == "none") {
        content.style.display = "block";
    } else {
        content.style.display = "none";
    }
}

// JS function for expanding the classlist within a cluster
function showClassesInCluster(level, runId) {
    var content = document.getElementById("classes_" + level + "_" + runId);
    if (content.style.display == "none") {
        content.style.display = "block";
    } else {
        content.style.display = "none";
    }
}

// JS function for showing the descriptions from the individual Visualizations
function showVisualisationDescriptions(text) {
    desc_win = window.open("", "", "width=600,height=300,scrollbars=yes,resizable=yes");
    desc_win.document.write(text);
    desc_win.document.close();
}

// JS helper function for initializing the SDH, TP and TW array
function initSDH() {
    for (i = 0; i < sdhArray.length; i++) {
        sdhArray[i] = 1;
    }
}

function initTP() {
    for (i = 0; i < tpArray.length; i++) {
        tpArray[i] = 1;
    }
}

function initTW() {
    for (i = 0; i < twArray.length; i++) {
        twArray[i] = 1;
    }
}

function setMaxAnzahlSDH(max) {
    maxSDHAnzahl = max
}

function setMaxAnzahlTW(max) {
    maxTWAnzahl = max
}

function setMaxAnzahlTP(max) {
    maxTPAnzahl = max
}

// JSfunction for showing the SDH,TP & TW Images
function nextSDH(runId, step) {
    if (sdhArray[runId] >= maxSDHAnzahl) {
        document.getElementById("sdh_" + runId + "_" + sdhArray[runId]).style.display = 'none';
        document.getElementById("sdh_" + runId + "_" + 1).style.display = 'block';
        sdhArray[runId] = 1;
    } else {
        document.getElementById("sdh_" + runId + "_" + sdhArray[runId]).style.display = 'none';
        document.getElementById("sdh_" + runId + "_" + (sdhArray[runId] + step)).style.display = 'block';
        sdhArray[runId] += step;
    }
}

function prevSDH(runId, step) {
    if (sdhArray[runId] <= 1) {
        document.getElementById("sdh_" + runId + "_" + 1).style.display = 'none';
        document.getElementById("sdh_" + runId + "_" + maxSDHAnzahl).style.display = 'block';
        sdhArray[runId] = maxSDHAnzahl;
    } else {
        document.getElementById("sdh_" + runId + "_" + sdhArray[runId]).style.display = 'none';
        document.getElementById("sdh_" + runId + "_" + (sdhArray[runId] - step)).style.display = 'block';
        sdhArray[runId] -= step;
    }
}

function nextTP(runId, step) {
    if (tpArray[runId] >= maxTPAnzahl) {
        document.getElementById("tp_" + runId + "_" + tpArray[runId]).style.display = 'none';
        document.getElementById("tp_" + runId + "_" + 1).style.display = 'block';
        tpArray[runId] = 1;
    } else {
        document.getElementById("tp_" + runId + "_" + tpArray[runId]).style.display = 'none';
        document.getElementById("tp_" + runId + "_" + (tpArray[runId] + step)).style.display = 'block';
        tpArray[runId] += step;
    }
}

function prevTP(runId, step) {
    if (tpArray[runId] <= 1) {
        document.getElementById("tp_" + runId + "_" + 1).style.display = 'none';
        document.getElementById("tp_" + runId + "_" + maxTPAnzahl).style.display = 'block';
        tpArray[runId] = maxTPAnzahl;
    } else {
        document.getElementById("tp_" + runId + "_" + tpArray[runId]).style.display = 'none';
        document.getElementById("tp_" + runId + "_" + (tpArray[runId] - step)).style.display = 'block';
        tpArray[runId] -= step;
    }
}

function nextTW(runId, step) {
    if (twArray[runId] >= maxTWAnzahl) {
        document.getElementById("tw_" + runId + "_" + twArray[runId]).style.display = 'none';
        document.getElementById("tw_" + runId + "_" + 1).style.display = 'block';
        twArray[runId] = 1;
    } else {
        document.getElementById("tw_" + runId + "_" + twArray[runId]).style.display = 'none';
        document.getElementById("tw_" + runId + "_" + (twArray[runId] + step)).style.display = 'block';
        twArray[runId] += step;
    }
}

function prevTW(runId, step) {
    if (twArray[runId] <= 1) {
        document.getElementById("tw_" + runId + "_" + 1).style.display = 'none';
        document.getElementById("tw_" + runId + "_" + maxTWAnzahl).style.display = 'block';
        twArray[runId] = maxTWAnzahl;
    } else {
        document.getElementById("tw_" + runId + "_" + twArray[runId]).style.display = 'none';
        document.getElementById("tw_" + runId + "_" + (twArray[runId] - step)).style.display = 'block';
        twArray[runId] -= step;
    }
}

// Functions to display Semantic Classes
function showSemanticClassesinRegion(text, index, runId) {
    var content = document.getElementById("classes_" + text + "_" + index + "_" + runId);
    if (content.style.display == "none") {
        content.style.display = "block";
    } else {
        content.style.display = "none";
    }
}

function showSemanticDescription(text) {
    top.consoleRef = window.open('', 'myconsole', 'width=450', 'height=550', 'menubar=0', 'toolbar=1', 'status=0',
            'scrollbars=1', 'resizable=1');
    top.consoleRef.document.writeln('<html><head><title>Console</title></head>'
            + '<script LANGUAGE="JavaScript" TYPE="text/javascript" src="reporter.js"></script>'
            + '<body bgcolor=white onLoad="self.focus()">' + text + '</body></html>');
    top.consoleRef.document.close();
}

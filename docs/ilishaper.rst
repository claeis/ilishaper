======================
ilishaper-Anleitung
======================

Überblick
=========

ilishaper ist ein in Java erstelltes Programm, um 
nicht-öffentliche Attribute und Objekte resp. nicht-öffentliche Klasse 
abzustreifen.

Es bestehen u.a. folgende Funktionen:

- Erstellen eines vereinfachten Modells anhand von einem existierenden Modell (ili-Dateien)
- Transformiert auf Basis beider Modelle ein bestehendes XTF in ein reduziertes XTF


Log-Meldungen
-------------
Die Log-Meldungen sollen dem Benutzer zeigen, was das Programm macht.
Am Anfang erscheinen Angaben zur Programm-Version.::
	
  Info: ilishaper-1.0.0
  ...
  Info: compile models...
  ...
  Info: ...clone done

Bei einem Fehler wird das am Ende des Programms vermerkt. Der eigentliche 
Fehler wird aber in der Regel schon früher ausgegeben.::
	
  Info: ilishaper-1.0.0
  ...
  Info: compile models...
  ...
  Error: failed to read ilimodels.xml
  ...
  Error: ...conversion failed

Laufzeitanforderungen
---------------------

Das Programm setzt Java 1.8 voraus.

Lizenz
------

GNU Lesser General Public License

Funktionsweise
==============

In den folgenden Abschnitten wird die Funktionsweise anhand einzelner
Anwendungsfälle beispielhaft beschrieben. Die detaillierte Beschreibung
einzelner Funktionen ist im Kapitel „Referenz“ zu finden.

Es existiert aktuell kein GUI. 
Das Programm kann nur über die Kommandozeile benutzt werden.

Modell-Konvertierungsfunktionen
---------------------------------------

Fall 1.1
~~~~~~~~

Ausgehend von einem existierenden Modell ``Basismodell.ili`` wird anhand 
der Konfiguration ``Beispiel1.ini`` ein vereinfachtes Modell 
``Derivatmodell.ili`` erstellt.

``java -jar ilishaper.jar --createModel --config Beispiel1.ini --out Derivatmodell.ili Basismodell.ili``

In der Konfigurationsdatei muss konfiguriert werden, wie das vereinfachte Modell
heissen soll::

    [Basismodell]             # Name des Quellmodells
    name=Derivatmodell        # Name des vereinfachten Modells
    version=2023-01-01        # Version des vereinfachten Modells

Einzelne Modellelemente, z.B. ein Attribut, können abgestreift werden::

    [Basismodell.TopicT1.ClassA.Attr1]  # qualifizierter Name des Attributes
    ignore=true                         # Flag, um das Attribut abzustreifen

Daten-Konvertierungsfunktionen
------------------------------------

Fall 2.1
~~~~~~~~

Ausgehend von existierenden, vollständigen Daten ``Basisdaten.xtf`` wird anhand 
der Konfiguration ``Beispiel1.ini`` ein vereinfachter, reduzierter Datensatz 
``Derivatdaten.xtf`` (passend zum vereinfachten Modell 
``Derivatmodell.ili``) erstellt.

``java -jar ilishaper.jar --deriveData --config Beispiel1.ini --out Derivatdaten.xtf Basisdaten.xtf``


Referenz
========

In den folgenden Abschnitten werden einzelne Aspekte detailliert, aber
isoliert, beschrieben. Die Funktionsweise als Ganzes wird anhand
einzelner Anwendungsfälle beispielhaft im Kapitel „Funktionsweise“
(weiter oben) beschrieben.

Aufruf-Syntax
-------------

``java -jar ilishaper.jar [Options] file...``

Es existiert aktuell kein GUI. 
Das Programm kann nur über die Kommandozeile benutzt werden.

Der Rückgabewert ist wie folgt:

  - 0 Funktion ok, keine Fehler festgestellt
  - !0 Funktion nicht ok, Fehler festgestellt

Optionen:

+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Option                                      | Beschreibung                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
+=============================================+========================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================+
| ``--createModel``                           | Erstellt ein neues Modell (ili-Datei). Zwingende weitere Angaben: Konfigurationsdatei (``--config``) und Ausgabedatei (``--out``)                                                                                                                                                                                                                                                                                                                                                                                                      |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--deriveData``                            | Erstellt ein neue, reduzierte Daten-Datei (xtf-Datei). Zwingende weitere Angaben: Quelldatei (``file``), Konfigurationsdatei (``--config``) und Ausgabedatei (``--out``)                                                                                                                                                                                                                                                                                                                                                               |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--modeldir path``                         | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default ist                                                                                                                                                                                                                                                                                                                                                       |
|                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                             | %ITF\_DIR;http://models.interlis.ch/;%JAR\_DIR/ilimodels                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                             | %ITF\_DIR ist ein Platzhalter für das Verzeichnis mit der Transferdatei.                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                             | %JAR\_DIR ist ein Platzhalter für das Verzeichnis des ilishaper Programms (ilishaper.jar Datei).                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                             | Der erste Modellname (Hauptmodell), zu dem ilishaper die ili-Datei sucht, ist nicht von der INTERLIS-Sprachversion abhängig. Es wird in folgender Reihenfolge nach einer ili-Datei gesucht: zuerst INTERLIS 2.3, dann 1.0 und zuletzt 2.2.                                                                                                                                                                                                                                                                                             |
|                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                             | Beim Auflösen eines IMPORTs wird die INTERLIS Sprachversion des Hauptmodells berücksichtigt, so dass also z.B. das Modell Units für ili2.2 oder ili2.3 unterschieden wird.                                                                                                                                                                                                                                                                                                                                                             |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--out filename``                          | Datei die erstellt oder überschrieben wird.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--config filename``                       | Konfiguriert die Konvertierung mit Hilfe einer INI-Datei.                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--log filename``                          | Schreibt die log-Meldungen in eine Text-Datei.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--proxy host``                            | Proxy Server für den Zugriff auf Modell Repositories                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--proxyPort port``                        | Proxy Port für den Zugriff auf Modell Repositories                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--trace``                                 | Erzeugt zusätzliche Log-Meldungen (wichtig für Programm-Fehleranalysen)                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--help``                                  | Zeigt einen kurzen Hilfetext an.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``--version``                               | Zeigt die Version des Programmes an.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
+---------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

Konfiguration
-------------
Die Abbildung muss in einer Konfigurations-Datei definiert werden.

Es muss konfiguriert werden, wie das vereinfachte Modell
heissen soll::

    [Basismodell]             # Name des Quellmodells
    name=Derivatmodell        # Name des vereinfachten Modells
    version=2023-01-01        # Version des vereinfachten Modells

Einzelne Modellelemente, z.B. ein Attribut, können abgestreift werden::

    [Basismodell.TopicT1.ClassA.Attr1]  # qualifizierter Name des Attributes
    ignore=true                         # Flag, um das Attribut abzustreifen

Es können auch ganze Klassen oder Topics abgestreift werden::

    [Basismodell.TopicT1.ClassB]  # qualifizierter Name der Klasse
    ignore=true

    [Basismodell.TopicT2]         # qualifizierter Name des Topics
    ignore=true

Es können auch nur bestimmte Objekte gefiltert werden:: 

    [Basismodell.TopicT1.ClassA]
    filter="Attr5==#rot"

Alle Objekte der Klasse ``ClassA`` welche im Attribut ``Attr5`` den Wert ``#rot`` haben
(alle Objekte die die Filterbedingung erfüllen), werden in die Ausgabedatei geschrieben.

INI-Konfigurationsdatei
~~~~~~~~~~~~~~~~~~~~~~~~
`Beispiel1.ini`_

.. _Beispiel1.ini: Beispiel1.ini

+------------------+--------------------------+-----------------------------------------------------------------------------------+
| Modelelement     | Konfiguartion            | Beschreibung                                                                      |
+==================+==========================+===================================================================================+
| ModelDef         | ::                       | Definiert den Namen des neu zu erstellenden Modells                               |
|                  |                          |                                                                                   |
|                  |  name                    |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell]                                                                   |
|                  |                          |   name=Derivatmodell                                                              |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ModelDef         | ::                       | Definiert den Herausgeber des neu zu erstellenden Modells. Das muss eine URI sein.|
|                  |                          |                                                                                   |
|                  |  issuer                  |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell]                                                                   |
|                  |                          |   issuer=mailto:user@host                                                         |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ModelDef         | ::                       | Definiert die Angabe zur VERSION des neu zu erstellenden Modells.                 |
|                  |                          |                                                                                   |
|                  |  version                 |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell]                                                                   |
|                  |                          |   version=1.0                                                                     |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ModelDef         | ::                       | Definiert die optionale Angabe Erläuterung zur VERSION des neu                    |
|                  |                          | zu erstellenden Modells.                                                          |
|                  |  versionExpl             |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell]                                                                   |
|                  |                          |   versionExpl=Entwurf                                                             |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ModelDef         | ::                       | Definiert den Modell-Kommentar des neu                                            |
|                  |                          | zu erstellenden Modells.                                                          |
|                  |  doc                     |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell]                                                                   |
|                  |                          |   doc=Kommentar zum neuen Modell                                                  |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ModelDef         | ::                       | Falls in den Filter-Ausdrücken Funktionen benutzt werden, die nicht schon         |
|                  |                          | im Ausgangsmodell importiert werden, müssen diese hier definiert werden.          |
|                  |  filterModels            |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell]                                                                   |
|                  |                          |   filterModels=Text;Math                                                          |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| TopicDef         | ::                       | Falls true wird das entsprechende Topic ignoriert.                                |
|                  |                          |                                                                                   |
|                  |  ignore                  |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell.TopicT2]                                                           |
|                  |                          |   ignore=true                                                                     |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ClassDef         | ::                       | Falls true wird die entsprechende Klasse, Struktur, Assoziation                   |
| StructureDef     |                          | oder Sicht ignoriert.                                                             |
| AssociationDef   |  ignore                  |                                                                                   |
| ViewDef          |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell.TopicT2.ClassB]                                                    |
|                  |                          |   ignore=true                                                                     |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| ClassDef         | ::                       | Ein Ausdruck (Syntax-Regel Expression; wie bei einem Mandatory-Constraint).       |
| StructureDef     |                          | Falls die Auswertung des Ausdrucks true ergibt, wird das entsprechende Objekt     |
| AssociationDef   |  filter                  | in die Ausgabe kopiert, falls false (oder der Ausdruck nicht auswertbar           |
| ViewDef          |                          | ist (z.B. Division mit 0)), wird das Objekt ignoriert.                            |
|                  |                          |                                                                                   |
|                  |                          | Der Ausdruck muss pro Objekt (ohne Beizug anderer Objekte) auswertbar             |
|                  |                          | sein (darf also keine Rollen oder Referenzattribute enthalten).                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell.TopicT1.ClassA]                                                    |
|                  |                          |   filter="Attr5==#rot"                                                            |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+
| AttributeDef     | ::                       | Falls true wird das entsprechende Attribut                                        |
|                  |                          | ignoriert.                                                                        |
|                  |  ignore                  |                                                                                   |
|                  |                          |                                                                                   |
|                  |                          | Beispiel                                                                          |
|                  |                          |                                                                                   |
|                  |                          | ::                                                                                |
|                  |                          |                                                                                   |
|                  |                          |   [Basismodell.TopicT2.ClassA.Attr1]                                              |
|                  |                          |   ignore=true                                                                     |
|                  |                          |                                                                                   |
+------------------+--------------------------+-----------------------------------------------------------------------------------+

# Validatore Scripts

In fase di startup dell'engine e come parte di un tool separato (ValidScript.java) vengono validati tutti gli script json degli Script. 
Lanciando il tool separato viene lanciata la validazione per ogni script presente nella cartella scripts. 
L'azione si ferma sul primo errore trovato partendo dall'inizio alla fine di ogni file, i quali a loro volta vengono validati in ordine alfabetico. 

## Documentazione comandi

* **hurtPlayer**: Danneggia il Personaggio in questo step.
  * ***damage**: (necessario) Danno da infliggere al Personaggio.
  * **canKillPlayer**: Indica se questo danno può uccidere il Personaggio. Se è false, non verrà inflitto danno quando il Personaggio non ha più punti vita.
* **invisible**: In questo step non renderizzerà l'attore.
* **textBoxKey**: Chiave di una textbox per questo step
* **renderOnlyMapLayer**: In questo step renderizzerà solo i tile del map_layer indicato
* **move**: oggetto composto da due campi
    * **direction**: (necessario) Direzione in cui l'echoactor si muove in questo step.
    * **speed**: (necessario) Velocità in cui l'echoactor si muove in questo step.
* **goTo**: oggetto composto da più campi. Solo *step* o *end* (non entrambi contemporaneamente) e uno degli altri campi è necessario.
    * **step**: (necessario) Step dello script in cui saltare.
    * **end**: termina lo script.
    * **ifAtLeastOneKillableAlive**: Innesca il goTo se c'è una GameInstance di tipo Killable che non sia morta nella stanza.
    * **ifPlayerDamageIsMoreThan**: Innesca il goTo se il Personaggio ha un numero di Danni maggiore rispetto al parametro.
    * **ifNoKillableAlive**: Innesca il goTo se NON c'è una GameInstance di tipo Killable viva nella stanza.
    * **ifAtLeastOnePOIExaminable**: Innesca il goTo se c'è una POIInstance di un certo tipo ancora non esaminata nella stanza.
    * **ifPlayerDamageIsLessThan**: Innesca il goTo se il Personaggio ha un numero di Danni minore rispetto al parametro.
    * **checkOnEveryFrame**: controlla ad ogni frame, invece che a fine step, le condizioni inserite. Permette di tagliare i tempi di innesco del "goTo". 
    * **onlyOneConditionMustBeTrue**: Solo una condizione in questo deve essere vera per innescare il "goTo".
* **spawn**: oggetto composto da più campi. genera una GameInstance in questo step
    * **identifier**: (necessario) instance da generare.
    * **x**: posizione X dove verrà generata. Di default è quella dell'Echoactor.
    * **y**: posizione Y dove verrà generata. Di default è quella dell'Echoactor.
    * **relative**: se true, le coordinate qui sopra sono aggiunte algebricamente alla posizone dell'Echoactor
* **splashToShow**: splash da mostrare fino alla fine dello step.
* **useAnimationOfStep**: utilizza per questo step l'animazione dello step indicato come parametro. Utile per riciclare animazioni e diminuire le dimensioni degli spritesheet

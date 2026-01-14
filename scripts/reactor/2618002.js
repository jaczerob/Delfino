
/*2618002.js - MagatiaPQ Door
 *@author Ronan
 */

function act() {
    var eim = rm.getEventInstance();

    var isAlcadno = eim.getIntProperty("isAlcadno");
    var reactname = (isAlcadno == 0) ? "rnj31_out" : "jnr31_out";
    var reactmap = (isAlcadno == 0) ? 926100201 : 926110201;

    eim.getMapInstance(reactmap).getReactorByName(reactname).hitReactor(rm.getClient());
}
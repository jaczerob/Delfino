
/*2618001.js - MagatiaPQ Door
 *@author Ronan
 */

function act() {
    var eim = rm.getEventInstance();

    var isAlcadno = eim.getIntProperty("isAlcadno");
    var reactname = (isAlcadno == 0) ? "rnj32_out" : "jnr32_out";
    var reactmap = (isAlcadno == 0) ? 926100202 : 926110202;

    eim.getMapInstance(reactmap).getReactorByName(reactname).hitReactor(rm.getClient());
}
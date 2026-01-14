
/*2619000.js - Zenumist crystal
 *@author Ronan
 */

function hit() {
    rm.dropItems();
}

function act() {
    // There's a timeout of 3 seconds to revert back from state 1 to 0.
    // Reactor is destroyed (state 2) and triggers this if dropping two Magic Devices at once, which shouldn't really happen.
}
/**
    Tombstone in Forest of Dead Trees IV
    MSEA reference: http://mymapleland.blogspot.com/2009/09/kill-lich-at-forest-of-dead-trees-i-to.html
*/
function hit() {
    if (rm.getReactor().getState() !== 0) {
        return
    }

    rm.weakenAreaBoss(6090000, "As the tombstone lit up and vanished, Lich lost all his magic abilities.")
}

function act() {
    // If the chest is destroyed before Riche, killing him should yield no exp
}
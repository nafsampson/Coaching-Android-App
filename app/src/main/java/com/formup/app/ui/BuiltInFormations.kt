// Defines the standard formations and their pitch positions.

package com.formup.app.ui

import com.formup.app.data.FormationSlot
import com.formup.app.data.SavedFormation

internal val builtInFormations = listOf(

        SavedFormation(
            id = "builtin-433",
            name = "Attacking",
            code = "4-3-3",
            positions = listOf(
                FormationSlot("gk", "GK", 50.0, 90.0),
                FormationSlot("lb", "LB", 18.0, 75.0),
                FormationSlot("lcb", "LCB", 39.0, 78.0),
                FormationSlot("rcb", "RCB", 61.0, 78.0),
                FormationSlot("rb", "RB", 82.0, 75.0),
                FormationSlot("lcm", "LCM", 32.0, 58.0),
                FormationSlot("cm", "CM", 50.0, 62.0),
                FormationSlot("rcm", "RCM", 68.0, 58.0),
                FormationSlot("lw", "LW", 20.0, 30.0),
                FormationSlot("st", "ST", 50.0, 22.0),
                FormationSlot("rw", "RW", 80.0, 30.0)
            )
        ),

        SavedFormation(
            id = "builtin-442",
            name = "Classic",
            code = "4-4-2",
            positions = listOf(
                FormationSlot("gk", "GK", 50.0, 90.0),
                FormationSlot("lb", "LB", 18.0, 75.0),
                FormationSlot("lcb", "CB", 39.0, 78.0),
                FormationSlot("rcb", "CB", 61.0, 78.0),
                FormationSlot("rb", "RB", 82.0, 75.0),
                FormationSlot("lm", "LM", 18.0, 55.0),
                FormationSlot("lcm", "CM", 39.0, 58.0),
                FormationSlot("rcm", "CM", 61.0, 58.0),
                FormationSlot("rm", "RM", 82.0, 55.0),
                FormationSlot("lst", "ST", 38.0, 25.0),
                FormationSlot("rst", "ST", 62.0, 25.0)
            )
        ),

        SavedFormation(
            id = "builtin-4231",
            name = "Balanced",
            code = "4-2-3-1",
            positions = listOf(
                FormationSlot("gk", "GK", 50.0, 90.0),
                FormationSlot("lb", "LB", 18.0, 75.0),
                FormationSlot("lcb", "LCB", 39.0, 78.0),
                FormationSlot("rcb", "RCB", 61.0, 78.0),
                FormationSlot("rb", "RB", 82.0, 75.0),
                FormationSlot("ldm", "LDM", 40.0, 64.0),
                FormationSlot("rdm", "RDM", 60.0, 64.0),
                FormationSlot("lam", "LAM", 20.0, 42.0),
                FormationSlot("cam", "CAM", 50.0, 38.0),
                FormationSlot("ram", "RAM", 80.0, 42.0),
                FormationSlot("st", "ST", 50.0, 20.0)
            )
        )
    )

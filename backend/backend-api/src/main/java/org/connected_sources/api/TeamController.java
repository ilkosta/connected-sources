package org.connected_sources.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    @PostMapping("/create")
    public String createTeam() {
        return "Team created";
    }

    @PutMapping("/modify")
    public String modifyTeam() {
        return "Team modified";
    }
}
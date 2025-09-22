package org.connected_sources.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repo")
public class RepoCtrl {
    public String create(String basedir, String name) {
        return "ContentRepo created at " + basedir + "/" + name;
    }
}
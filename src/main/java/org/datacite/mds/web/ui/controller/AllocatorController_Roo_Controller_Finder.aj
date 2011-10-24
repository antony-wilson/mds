// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.datacite.mds.web.ui.controller;

import java.lang.String;
import org.datacite.mds.domain.Allocator;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

privileged aspect AllocatorController_Roo_Controller_Finder {
    
    @RequestMapping(params = { "find=ByNameLike", "form" }, method = RequestMethod.GET)
    public String AllocatorController.findAllocatorsByNameLikeForm(Model uiModel) {
        return "allocators/findAllocatorsByNameLike";
    }
    
    @RequestMapping(params = "find=ByNameLike", method = RequestMethod.GET)
    public String AllocatorController.findAllocatorsByNameLike(@RequestParam("name") String name, Model uiModel) {
        uiModel.addAttribute("allocators", Allocator.findAllocatorsByNameLike(name).getResultList());
        return "allocators/list";
    }
    
    @RequestMapping(params = { "find=BySymbolEquals", "form" }, method = RequestMethod.GET)
    public String AllocatorController.findAllocatorsBySymbolEqualsForm(Model uiModel) {
        return "allocators/findAllocatorsBySymbolEquals";
    }
    
}
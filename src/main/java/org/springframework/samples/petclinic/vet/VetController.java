/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private static final String VIEWS_VETS_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateVetForm";

	private final VetRepository vets;

	public VetController(VetRepository clinicService) {
		this.vets = clinicService;
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model);

	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vets.findAll(pageable);
	}

	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vets.findAll());
		return vets;
	}

	@GetMapping("/vets/new")
	public String initCreationForm(Map<String, Object> model) {
		Vet vet = new Vet();
		model.put("vet", vet);
		return VIEWS_VETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/vets/new")
	public String processCreationForm(@Valid Vet vet, BindingResult result) {
		if (result.hasErrors())
			return VIEWS_VETS_CREATE_OR_UPDATE_FORM;

		if (this.vets.existsByEmail(vet.getEmail())) {
			result.rejectValue("email", "vet.email", "An account already exists for this email.");
			return VIEWS_VETS_CREATE_OR_UPDATE_FORM;
		}

		this.vets.save(vet);
		return "redirect:/vets/" + vet.getId();
	}

	// TODO - implement edit details for a vet
	// @GetMapping("/vets/{vetId}/edit")
	// public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model
	// model) {
	// Owner owner = this.vets.findById(ownerId);
	// model.addAttribute(owner);
	// return VIEWS_VETS_CREATE_OR_UPDATE_FORM;
	// }

	// TODO - implement post processing for udpateing details of a vet
	// @PostMapping("/vets/{vetId}/edit")
	// public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result,
	// @PathVariable("ownerId") int ownerId) {
	// if (result.hasErrors()) {
	// return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	// }
	// else {
	// owner.setId(ownerId);
	// this.owners.save(owner);
	// return "redirect:/owners/{ownerId}";
	// }
	// }

	/**
	 * Custom handler for displaying an owner.
	 * @param vetId the ID of the vet to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/vets/{vetId}")
	public ModelAndView showVet(@PathVariable("vetId") int vetId) {
		ModelAndView mav = new ModelAndView("vets/vetDetails");
		Vet vet = this.vets.findById(vetId);
		// for (Pet pet : owner.getPets()) {
		// pet.setVisitsInternal(visits.findByPetId(pet.getId()));
		// }
		mav.addObject(vet);
		return mav;
	}

}

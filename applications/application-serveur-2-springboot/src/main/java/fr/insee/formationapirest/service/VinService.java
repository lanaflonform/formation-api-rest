package fr.insee.formationapirest.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import fr.insee.formationapirest.exception.VinInconnuException;
import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;

@Service
public class VinService {
	
	@Autowired
	VinRepository vinRepository;
	
	public List<Vin> getAll(){
		return vinRepository.findAll();
	}
	
	public List<Vin> findByAppellation(String appellation){
		return vinRepository.findByAppellation(appellation);
	}
	
	public Vin getById(Integer id){
		return vinRepository.findById(id).orElse(null);
	}
	
	public void deleteById(@PathVariable Integer id){
		if(vinRepository.existsById(id)) { // renvoie un boolean (true si l'objet existe, false sinon)
			vinRepository.deleteById(id);
		} else {
			throw new VinInconnuException("le vin avec l'id "+ id + " n'existe pas");
		}
	}
	
	public Vin add(Vin vin){
	    // ajouter un contrôle pour s'assurer que l'id n'est pas renseigné ou passer par un DTO
		return vinRepository.save(vin);
	}
	
	public Vin update(Vin vin){
		if(vinRepository.existsById(vin.getId())) {
			return vinRepository.save(vin);
		}
		return null;
	}
	
	public Page<Vin> pageable(Pageable p) {
		return vinRepository.findAll(p);
	}
	
}
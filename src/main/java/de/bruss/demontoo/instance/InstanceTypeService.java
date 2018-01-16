package de.bruss.demontoo.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class InstanceTypeService {
    @Autowired
    InstanceTypeRepository instancetypeRepository;

    @Transactional
    public InstanceType findOne(Long id) {
        return instancetypeRepository.findOne(id);
    }

    @Transactional
    public void save(InstanceType instancetype) {
        instancetypeRepository.save(instancetype);
    }

    @Transactional
    public void create(InstanceType instancetype) {
        instancetypeRepository.save(instancetype);
    }

    @Transactional
    public void delete(Long id) {
        instancetypeRepository.delete(id);
    }

    @Transactional
    public List<InstanceType> findAll() {
        return instancetypeRepository.findAll();
    }

    @Transactional
    public InstanceType findByName(String name) {
        return instancetypeRepository.findByName(name);
    }
}
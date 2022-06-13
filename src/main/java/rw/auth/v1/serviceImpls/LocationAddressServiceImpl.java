package rw.auth.v1.serviceImpls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.auth.v1.dtos.GetAllLocationsDTO;
import rw.auth.v1.enums.ELocationType;
import rw.auth.v1.exceptions.BadRequestException;
import rw.auth.v1.exceptions.ResourceNotFoundException;
import rw.auth.v1.models.LocationAddress;
import rw.auth.v1.repositories.ILocationAddressRepository;
import rw.auth.v1.services.ILocationAddressService;


import java.util.List;
import java.util.Optional;

@Service
public class LocationAddressServiceImpl implements ILocationAddressService {
    private final ILocationAddressRepository locationAddressRepository;

    @Autowired
    public LocationAddressServiceImpl(ILocationAddressRepository locationAddressRepository) {
        this.locationAddressRepository = locationAddressRepository;
    }

    @Override
    public LocationAddress findById(Long id) {
        return locationAddressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", "id", id.toString()));
    }

    @Override
    public boolean existsById(Long id) {
        return locationAddressRepository.existsById(id);
    }

    @Override
    public List<LocationAddress> findAllByParentIdAndLocationType(LocationAddress province, ELocationType district) {
        return null;
    }

    @Override
    public LocationAddress findVillageById(Long id) {
        LocationAddress newLocationAddress = locationAddressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", "id", id.toString()));;
        if(newLocationAddress.getLocationType()!= ELocationType.VILLAGE){
            throw new BadRequestException(String.format("Location must be a Village"));
        }

        return newLocationAddress;
    }

    @Override
    public LocationAddress findCellById(Long id) {
        LocationAddress newLocationAddress = locationAddressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", "id", id.toString()));;
        if(newLocationAddress.getLocationType()!= ELocationType.CELL){
            throw new BadRequestException(String.format("Location must be a CELL"));
        }

        return newLocationAddress;
    }

    @Override
    public LocationAddress findSectorById(Long id) {
        LocationAddress newLocationAddress = locationAddressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", "id", id.toString()));;
        if(newLocationAddress.getLocationType()!= ELocationType.SECTOR){
            throw new BadRequestException(String.format("Location must be a Sector"));
        }

        return newLocationAddress;
    }

    @Override
    public LocationAddress findDistrictById(Long id) {
        LocationAddress newLocationAddress = locationAddressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", "id", id.toString()));;
        if(newLocationAddress.getLocationType()!= ELocationType.DISTRICT){
            throw new BadRequestException(String.format("Location must be a District"));
        }

        return newLocationAddress;
    }

    @Override
    public LocationAddress findProvinceById(Long id) {
        LocationAddress newLocationAddress = locationAddressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", "id", id.toString()));;
        if(newLocationAddress.getLocationType()!= ELocationType.PROVINCE){
            throw new BadRequestException(String.format("Location must be a Province"));
        }

        return newLocationAddress;
    }

    @Override
    public LocationAddress findDistrictOfVillageId(Long id) {
        LocationAddress village = findVillageById(id);
        LocationAddress district = findDistrictById(village.getParentId().getParentId().getParentId().getId());
        return district;
    }

    @Override
    public LocationAddress findDistrictOfSectorId(Long id) {
        LocationAddress sector = findSectorById(id);
        LocationAddress district = findDistrictById(sector.getParentId().getId());
        return district;
    }

    @Override
    public LocationAddress findProvinceOfDistrictId(Long id) {
        LocationAddress district = findDistrictById(id);
        LocationAddress province = findProvinceById(district.getParentId().getId());
        return province;
    }

    @Override
    public Page<LocationAddress> findAllDistricts(Pageable pageable) {
        return locationAddressRepository.findAllByLocationType( ELocationType.DISTRICT,pageable);
    }

    @Override
    public List<LocationAddress> findAllDistricts() {
        return locationAddressRepository.findAllByLocationType(ELocationType.DISTRICT);
    }

    @Override
    public LocationAddress findParentById(Long id) {
        return null;
    }

    @Override
    public GetAllLocationsDTO findAllLocationByVillageId(Long id) {

        LocationAddress village = findVillageById(id);
        LocationAddress cell = findById(village.getParentId().getId());
        LocationAddress sector = findById(cell.getParentId().getId());
        LocationAddress district = findById(sector.getParentId().getId());
        LocationAddress province = findById(district.getParentId().getId());

        GetAllLocationsDTO dto =  new GetAllLocationsDTO();
        dto.setVillage(village);
        dto.setCell(cell);
        dto.setSector(sector);
        dto.setDistrict(district);
        dto.setProvince(province);

        return dto;
    }

    @Override
    public Optional<LocationAddress> findVillageFromNames(String village, String cell, String sector, String district) {
        return locationAddressRepository.findVillageFromNames(village,cell,sector,district);
    }
}

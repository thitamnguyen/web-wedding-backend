package com.example.demo.service;

import com.example.demo.dto.ServiceCategoryResponse;
import com.example.demo.dto.ServicePackageResponse;
import com.example.demo.model.ServiceCategory;
import com.example.demo.model.ServicePackage;
import com.example.demo.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository repository;

    // Constructor Injection
    public ServiceCategoryService(ServiceCategoryRepository repository) {
        this.repository = repository;
    }

    public List<ServiceCategoryResponse> getAllServices() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ServiceCategoryResponse mapToResponse(
            ServiceCategory category
    ) {

        ServiceCategoryResponse response =
                new ServiceCategoryResponse();

        response.setId(category.getCategoryCode());
        response.setTitle(category.getTitle());
        response.setTagline(category.getTagline());
        response.setImage(category.getImageUrl());
        response.setSubTitle(category.getSubTitle());
        response.setDesc(category.getDescription());

        List<ServicePackageResponse> packages =
                category.getPackages()
                        .stream()
                        .map(this::mapPackage)
                        .toList();

        response.setPackages(packages);

        return response;
    }

    private ServicePackageResponse mapPackage(
            ServicePackage servicePackage
    ) {

        ServicePackageResponse response =
                new ServicePackageResponse();

        response.setName(servicePackage.getName());

        NumberFormat vn =
                NumberFormat.getInstance(
                        new Locale("vi", "VN")
                );

        response.setPrice(
                vn.format(servicePackage.getPrice()) + "đ"
        );

        response.setOutfits(
                servicePackage.getOutfits()
        );

        response.setMakeup(
                servicePackage.getMakeup()
        );

        response.setDuration(
                servicePackage.getDuration()
        );

        response.setTeam(
                servicePackage.getTeam()
        );

        response.setProducts(
                servicePackage.getProducts()
        );

        return response;
    }
}
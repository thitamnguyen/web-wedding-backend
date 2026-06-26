package com.example.demo.service;

import com.example.demo.dto.ImageUploadResult;
import com.example.demo.dto.ServiceCategoryResponse;
import com.example.demo.dto.ServiceCategoryUpsertRequest;
import com.example.demo.dto.ServicePackageResponse;
import com.example.demo.dto.ServicePackageUpsertRequest;
import com.example.demo.model.ServiceCategory;
import com.example.demo.model.ServicePackage;
import com.example.demo.repository.ServiceCategoryRepository;
import com.example.demo.repository.ServicePackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepository;
    private final ServicePackageRepository packageRepository;
    private final CloudinaryService cloudinaryService;

    public ServiceCategoryService(
            ServiceCategoryRepository categoryRepository,
            ServicePackageRepository packageRepository,
            CloudinaryService cloudinaryService
    ) {
        this.categoryRepository = categoryRepository;
        this.packageRepository = packageRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> getAllServices() {
        return categoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ServiceCategory::getId))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> getAdminCategories() {
        return getAllServices();
    }

    @Transactional(readOnly = true)
    public List<ServicePackageResponse> getAdminPackages() {
        return packageRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ServicePackage::getId))
                .map(this::mapPackage)
                .toList();
    }

    @Transactional
    public ServiceCategoryResponse createCategory(ServiceCategoryUpsertRequest request) {
        ServiceCategory category = new ServiceCategory();
        applyCategoryRequest(category, request, null);
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public ServiceCategoryResponse updateCategory(Integer id, ServiceCategoryUpsertRequest request) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay danh muc dich vu voi ID: " + id));

        String oldPublicId = category.getPublicId();
        applyCategoryRequest(category, request, oldPublicId);
        ServiceCategory saved = categoryRepository.save(category);

        if (request.getImageFile() != null && !request.getImageFile().isEmpty() && oldPublicId != null && !oldPublicId.isBlank()) {
            cloudinaryService.deleteImage(oldPublicId);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay danh muc dich vu voi ID: " + id));

        cloudinaryService.deleteImage(category.getPublicId());
        categoryRepository.delete(category);
    }

    @Transactional
    public ServicePackageResponse createPackage(ServicePackageUpsertRequest request) {
        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay danh muc dich vu voi ID: " + request.getCategoryId()));

        ServicePackage servicePackage = new ServicePackage();
        applyPackageRequest(servicePackage, request, category);
        return mapPackage(packageRepository.save(servicePackage));
    }

    @Transactional
    public ServicePackageResponse updatePackage(Integer id, ServicePackageUpsertRequest request) {
        ServicePackage servicePackage = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay goi dich vu voi ID: " + id));

        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay danh muc dich vu voi ID: " + request.getCategoryId()));

        applyPackageRequest(servicePackage, request, category);
        return mapPackage(packageRepository.save(servicePackage));
    }

    @Transactional
    public void deletePackage(Integer id) {
        ServicePackage servicePackage = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay goi dich vu voi ID: " + id));
        packageRepository.delete(servicePackage);
    }

    private ServiceCategoryResponse mapToResponse(ServiceCategory category) {
        ServiceCategoryResponse response = new ServiceCategoryResponse();
        response.setDbId(category.getId());
        response.setId(category.getCategoryCode());
        response.setTitle(category.getTitle());
        response.setTagline(category.getTagline());
        response.setImage(category.getImageUrl());
        response.setSubTitle(category.getSubTitle());
        response.setDesc(category.getDescription());
        response.setPublicId(category.getPublicId());

        List<ServicePackageResponse> packages = Optional.ofNullable(category.getPackages())
                .orElse(List.of())
                .stream()
                .sorted(Comparator.comparing(ServicePackage::getId))
                .map(this::mapPackage)
                .toList();

        response.setPackages(packages);
        return response;
    }

    private ServicePackageResponse mapPackage(ServicePackage servicePackage) {
        ServicePackageResponse response = new ServicePackageResponse();
        response.setId(servicePackage.getId());
        response.setCategoryId(servicePackage.getCategory() != null ? servicePackage.getCategory().getId() : null);
        response.setCategoryTitle(servicePackage.getCategory() != null ? servicePackage.getCategory().getTitle() : null);
        response.setName(servicePackage.getName());

        NumberFormat vn = NumberFormat.getInstance(new Locale("vi", "VN"));
        response.setRawPrice(servicePackage.getPrice());
        response.setPrice(servicePackage.getPrice() == null ? "" : vn.format(servicePackage.getPrice()) + "đ");
        response.setOutfits(servicePackage.getOutfits());
        response.setMakeup(servicePackage.getMakeup());
        response.setDuration(servicePackage.getDuration());
        response.setTeam(servicePackage.getTeam());
        response.setProducts(servicePackage.getProducts());
        return response;
    }

    private void applyCategoryRequest(
            ServiceCategory category,
            ServiceCategoryUpsertRequest request,
            String existingPublicId
    ) {
        category.setCategoryCode(request.getCategoryCode());
        category.setTitle(request.getTitle());
        category.setTagline(request.getTagline());
        category.setSubTitle(request.getSubTitle());
        category.setDescription(request.getDescription());

        ImageUploadResult uploadResult = cloudinaryService.uploadImage(request.getImageFile(), "service-categories");
        if (uploadResult != null) {
            category.setImageUrl(uploadResult.getSecureUrl());
            category.setPublicId(uploadResult.getPublicId());
        } else if (existingPublicId != null) {
            category.setPublicId(existingPublicId);
        }
    }

    private void applyPackageRequest(
            ServicePackage servicePackage,
            ServicePackageUpsertRequest request,
            ServiceCategory category
    ) {
        servicePackage.setName(request.getName());
        servicePackage.setPrice(request.getPrice());
        servicePackage.setOutfits(request.getOutfits());
        servicePackage.setMakeup(request.getMakeup());
        servicePackage.setDuration(request.getDuration());
        servicePackage.setTeam(request.getTeam());
        servicePackage.setProducts(request.getProducts());
        servicePackage.setCategory(category);
    }
}

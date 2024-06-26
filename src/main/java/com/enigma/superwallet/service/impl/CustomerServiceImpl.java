package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.UpdateRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.UserCredentialResponse;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.DummyBank;
import com.enigma.superwallet.entity.ProfilePicture;
import com.enigma.superwallet.repository.CustomerRepository;
import com.enigma.superwallet.repository.DummyBankRepository;
import com.enigma.superwallet.repository.ProfileImageRepository;
import com.enigma.superwallet.service.CustomerService;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final DummyBankRepository dummyBankRepository;
    private final ProfileImageRepository profileImageRepository;
    private File file;

    @Value("${app.super_wallet.path.firebaseUrl}")
    private String firebaseJson;
    @Value("${app.super_wallet.firebase.bucketName}")
    private String bucketName;
    @Value("${app.super_wallet.firebase.downloadUrl}")
    private String DOWNLOAD_URL;

    @Override
    public CustomerResponse createCustomer(Customer customer) {
        Customer customer1 = customerRepository.saveAndFlush(customer);
        return CustomerResponse.builder()
                .id(customer1.getId())
                .firstName(customer1.getFirstName())
                .lastName(customer1.getLastName())
                .build();
    }

    @Override
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .filter(Customer::getIsActive)
                .map(customer -> CustomerResponse.builder()
                        .id(customer.getId())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .phoneNumber(customer.getPhoneNumber())
                        .birthDate(customer.getBirthDate())
                        .gender(customer.getGender())
                        .address(customer.getAddress())
                        .userCredential(UserCredentialResponse.builder()
                                .email(customer.getUserCredential().getEmail())
                                .role(customer.getUserCredential().getRole().getRoleName())
                                .build())
                        .build())
                .toList();
    }

    @Override
    public CustomerResponse getById(String id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer != null && customer.getIsActive()) {
            CustomerResponse.CustomerResponseBuilder responseBuilder = CustomerResponse.builder()
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .phoneNumber(customer.getPhoneNumber())
                    .birthDate(customer.getBirthDate())
                    .address(customer.getAddress())
                    .gender(customer.getGender())
                    .userCredential(UserCredentialResponse.builder()
                            .email(customer.getUserCredential().getEmail())
                            .role(customer.getUserCredential().getRole().getRoleName())
                            .pin(customer.getUserCredential().getPin())
                            .build())
                    .bankData(customer.getDummyBank());

            if (customer.getProfilePicture() != null) {
                responseBuilder.images(customer.getProfilePicture().getName());
            } else {
                responseBuilder.images(null);
            }

            return responseBuilder.build();
        }
        return null;
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public CustomerResponse update(UpdateRequest updateRequest) {
        try {
            Customer customer = customerRepository.findById(updateRequest.getId()).orElse(null);

            if (updateRequest.getProfilePictureRequest() != null) {

                String fileName = updateRequest.getProfilePictureRequest().getImage().getOriginalFilename();
                fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));

                file = this.convertToFile(updateRequest.getProfilePictureRequest().getImage(), fileName);
                String TEMP_URL = this.uploadFile(file, fileName);
                file.delete();

                ProfilePicture profilePicture = ProfilePicture.builder().name(TEMP_URL).uploadedAt(LocalDateTime.now()).build();
                profileImageRepository.saveAndFlush(profilePicture);
                customer.setProfilePicture(profilePicture);
            }

            Customer customer1 = Customer.builder()
                    .id(updateRequest.getId())
                    .createdAt(customer.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .firstName(updateRequest.getFirstName())
                    .lastName(updateRequest.getLastName())
                    .phoneNumber(updateRequest.getPhoneNumber())
                    .birthDate(LocalDate.parse(updateRequest.getBirthDate()))
                    .gender(updateRequest.getGender())
                    .address(updateRequest.getAddress())
                    .dummyBank(customer.getDummyBank())
                    .userCredential(customer.getUserCredential())
                    .isActive(customer.getIsActive())
                    .profilePicture(customer.getProfilePicture()) 
                    .build();

            customerRepository.save(customer1);
            String profilePictureName = null;
            if (customer1.getProfilePicture() != null) {
                profilePictureName = customer1.getProfilePicture().getName();
            }
            return CustomerResponse.builder()
                    .firstName(customer1.getFirstName())
                    .lastName(customer1.getLastName())
                    .phoneNumber(customer1.getPhoneNumber())
                    .birthDate(customer1.getBirthDate())
                    .gender(customer1.getGender())
                    .address(customer1.getAddress())
                    .bankData(customer1.getDummyBank())
                    .images(profilePictureName)
                    .build();

        } catch (Exception e) {
            if (file != null) {
                file.delete();
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @Override
    public Boolean delete(String id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer != null) {
            Customer deletedCustomer = Customer.builder()
                    .id(customer.getId())
                    .createdAt(customer.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .phoneNumber(customer.getPhoneNumber())
                    .birthDate(customer.getBirthDate())
                    .gender(customer.getGender())
                    .address(customer.getAddress())
                    .dummyBank(customer.getDummyBank())
                    .isActive(false)
                    .userCredential(customer.getUserCredential())
                    .build();
            customerRepository.save(deletedCustomer);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Customer> getCustomerByUserCredentialId(String id) {
        return customerRepository.findByUserCredentialId(id);
    }

    @Transactional
    @Override
    public void updateDummyBankId(String customerId, String dummyBankId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        DummyBank dummyBank = dummyBankRepository.findById(dummyBankId)
                .orElseThrow(() -> new RuntimeException("Dummy bank not found with ID: " + dummyBankId));

        customer.setDummyBank(dummyBank);

        // Save the updated customer entity
        customerRepository.save(customer);
    }

    private String uploadFile(File file, String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(firebaseJson));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        return String.format(DOWNLOAD_URL, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        return tempFile;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }


    @Override
    public Page<CustomerResponse> getCustomers(String fullName, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findAll(getCustomerSpecification(fullName), pageable)
                .map(this::mapToCustomerResponse);
    }

    private Specification<Customer> getCustomerSpecification(String fullName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fullName != null && !fullName.isEmpty()) {
                String[] names = fullName.split("\\s+");
                String firstName = names[0];
                String lastName = names.length > 1 ? names[1] : "";
                Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
                Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(firstNamePredicate, lastNamePredicate));
            }
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }



    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .birthDate(customer.getBirthDate())
                .address(customer.getAddress())
                .gender(customer.getGender())
                .userCredential(UserCredentialResponse.builder()
                        .email(customer.getUserCredential().getEmail())
                        .role(customer.getUserCredential().getRole().getRoleName())
                        .pin(customer.getUserCredential().getPin())
                        .build())
                .bankData(customer.getDummyBank())
                .images(customer.getProfilePicture() != null ? customer.getProfilePicture().getName() : null)
                .build();
    }

}

package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.CustomerDtos;
import com.wasac.billing.entity.Customer;
import com.wasac.billing.entity.User;
import com.wasac.billing.enums.EntityStatus;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.CustomerService;
import com.wasac.billing.utils.RwandaPhoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CustomerDtos.CustomerResponse create(CustomerDtos.CustomerRequest request) {
        validateUnique(request, null);
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Linked user not found"));
        }
        Customer customer = Customer.builder()
                .fullNames(request.getFullNames().trim())
                .nationalId(request.getNationalId())
                .email(request.getEmail().trim().toLowerCase())
                .phoneNumber(RwandaPhoneValidator.normalize(request.getPhoneNumber()))
                .address(request.getAddress().trim())
                .status(request.getStatus())
                .user(user)
                .build();
        Customer saved = customerRepository.save(customer);
        auditLogService.log("CREATE_CUSTOMER", "Customer", saved.getId(),
                "Created customer profile for " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerDtos.CustomerResponse update(Long id, CustomerDtos.CustomerRequest request) {
        Customer customer = findCustomer(id);
        validateUnique(request, id);
        customer.setFullNames(request.getFullNames().trim());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail().trim().toLowerCase());
        customer.setPhoneNumber(RwandaPhoneValidator.normalize(request.getPhoneNumber()));
        customer.setAddress(request.getAddress().trim());
        customer.setStatus(request.getStatus());
        if (request.getUserId() != null) {
            customer.setUser(userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Linked user not found")));
        } else {
            customer.setUser(null);
        }
        Customer saved = customerRepository.save(customer);
        auditLogService.log("UPDATE_CUSTOMER", "Customer", saved.getId(),
                "Updated customer profile for " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDtos.CustomerResponse getById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDtos.CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Customer customer = findCustomer(id);
        customer.setStatus(EntityStatus.INACTIVE);
        customerRepository.save(customer);
        auditLogService.log("DEACTIVATE_CUSTOMER", "Customer", customer.getId(),
                "Deactivated customer " + customer.getEmail());
    }

    private void validateUnique(CustomerDtos.CustomerRequest request, Long existingId) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = RwandaPhoneValidator.normalize(request.getPhoneNumber());
        customerRepository.findByEmail(email).filter(c -> !c.getId().equals(existingId))
                .ifPresent(c -> { throw new BusinessException("Customer email already exists", HttpStatus.CONFLICT); });
        customerRepository.findByNationalId(request.getNationalId()).filter(c -> !c.getId().equals(existingId))
                .ifPresent(c -> { throw new BusinessException("Customer national ID already exists", HttpStatus.CONFLICT); });
        customerRepository.findByPhoneNumber(phone).filter(c -> !c.getId().equals(existingId))
                .ifPresent(c -> { throw new BusinessException("Customer phone number already exists", HttpStatus.CONFLICT); });
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    private CustomerDtos.CustomerResponse toResponse(Customer customer) {
        return CustomerDtos.CustomerResponse.builder()
                .id(customer.getId())
                .fullNames(customer.getFullNames())
                .nationalId(customer.getNationalId())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .status(customer.getStatus())
                .userId(customer.getUser() == null ? null : customer.getUser().getId())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}

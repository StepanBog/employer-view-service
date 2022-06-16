package ru.bogdanov.diplom.backend.service.impl;

import com.vaadin.flow.data.provider.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.bogdanov.diplom.backend.data.containers.Employer;
import ru.bogdanov.diplom.backend.mapper.EmployerMapper;
import ru.bogdanov.diplom.backend.service.IEmployerService;
import ru.bogdanov.diplom.grpc.generated.service.employer.EmployerServiceGrpc;
import ru.bogdanov.diplom.grpc.generated.service.employer.EmployersResponse;
import ru.bogdanov.diplom.grpc.generated.service.employer.OneEmployerRequest;
import ru.bogdanov.diplom.grpc.generated.service.employer.SearchEmployerRequest;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * @author SBogdanov
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerService implements IEmployerService {

    @GrpcClient("main-service")
    private EmployerServiceGrpc.EmployerServiceBlockingStub employerClient;

    private final EmployerMapper employerMapper;

    public EmployersResponse find(final @NotNull SearchEmployerRequest request) {
        return employerClient.find(request);
    }

    @Override
    public List<Employer> findAll(@NotNull SearchEmployerRequest request) {
        return employerMapper.transform(find(request).getEmployersList());
    }

    @Override
    public List<Employer> find(Query<Employer, Employer> query, int pageSize) {
        SearchEmployerRequest request = employerMapper.transformToSearch(
                query.getFilter().orElse(null),
                query.getOffset() == 0 ? 0 : query.getOffset() / pageSize,
                pageSize
        );
        log.info("Find employers request - {}", request);
        EmployersResponse response = find(request);

        log.info("Find employers result - {}", response.getEmployersCount());
        return employerMapper.transform(response.getEmployersList());
    }

    @Override
    public Employer findById(@NotNull UUID employerId) {
        return employerMapper.transform(
                employerClient.findOne(
                        OneEmployerRequest.newBuilder()
                                .setEmployerId(employerId.toString())
                                .build()
                )
        );
    }

    @Override
    public Employer save(@NotNull Employer employer) {
        return employerMapper.transform(
                employerClient.save(
                        employerMapper.transform(employer)
                )
        );
    }

    @Override
    public int getTotalCount(Query<Employer, Employer> query) {
        SearchEmployerRequest request = employerMapper.transformToSearch(
                query.getFilter().orElse(null),
                0,
                5
        );
        log.info("Get total employers by request - {}", request);
        int result = (int) find(request).getTotalSize();
        log.info("Total employers by request - {}; result - {}", request, result);
        return result;
    }
}

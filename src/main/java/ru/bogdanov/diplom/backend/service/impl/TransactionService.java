package ru.bogdanov.diplom.backend.service.impl;

import com.vaadin.flow.data.provider.Query;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.bogdanov.diplom.backend.data.containers.Transaction;
import ru.bogdanov.diplom.backend.mapper.TransactionMapper;
import ru.bogdanov.diplom.backend.service.ITransactionService;
import ru.bogdanov.diplom.grpc.generated.ApproveRequest;
import ru.bogdanov.diplom.grpc.generated.DeclineRequest;
import ru.bogdanov.diplom.grpc.generated.service.transaction.FindByIdRequest;
import ru.bogdanov.diplom.grpc.generated.service.transaction.SearchTransactionRequest;
import ru.bogdanov.diplom.grpc.generated.service.transaction.TransactionSearchResponse;
import ru.bogdanov.diplom.grpc.generated.service.transaction.TransactionServiceGrpc;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * @author SBogdanov
 */
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    @GrpcClient("main-service")
    private TransactionServiceGrpc.TransactionServiceBlockingStub transactionClient;

    private final TransactionMapper transactionMapper;

    public TransactionSearchResponse find(final @NotNull SearchTransactionRequest request) {
        return transactionClient.find(request);
    }

    @Override
    public List<Transaction> find(Query<Transaction, Transaction> query, int pageSize) {
        SearchTransactionRequest request = transactionMapper.transformToSearch(
                query.getFilter().orElse(null),
                query.getOffset() == 0 ? 0 : query.getOffset() / pageSize,
                pageSize
        );
        TransactionSearchResponse response = find(request);
        return transactionMapper.transform(response.getTransactionsList());
    }

    @Override
    public Transaction findById(@NotNull UUID transactionID) {
        return transactionMapper.transform(
                transactionClient.findOneById(
                        FindByIdRequest.newBuilder()
                                .setTransactionId(transactionID.toString())
                                .build()
                )
        );
    }

    @Override
    public int getTotalCount(Query<Transaction, Transaction> query) {
        SearchTransactionRequest request = transactionMapper.transformToSearch(
                query.getFilter().orElse(null),
                0,
                5
        );
        return (int) find(request).getTotalSize();
    }

    @Override
    public void approve(Transaction transaction) {
        transactionClient.approveRequest(ApproveRequest.newBuilder()
                .setTransactionId(transaction.getId())
                .build());
    }

    @Override
    public void decline(Transaction transaction) {
        transactionClient.declineRequest(DeclineRequest.newBuilder()
                .setTransactionId(transaction.getId())
                .build());
    }
}

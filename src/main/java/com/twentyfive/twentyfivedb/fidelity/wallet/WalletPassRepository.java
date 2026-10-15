package com.twentyfive.twentyfivedb.fidelity.wallet;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletPassRepository extends MongoRepository<WalletPass, String> {
}

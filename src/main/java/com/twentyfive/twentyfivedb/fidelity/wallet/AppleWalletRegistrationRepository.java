package com.twentyfive.twentyfivedb.fidelity.wallet;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppleWalletRegistrationRepository extends MongoRepository<AppleWalletRegistration, String> {

    Optional<AppleWalletRegistration> findFirstByDeviceLibraryIdentifierAndSerialNumber(String deviceLibraryIdentifier, String serialNumber);

    List<AppleWalletRegistration> findAllByDeviceLibraryIdentifier(String deviceLibraryIdentifier);

    List<AppleWalletRegistration> findAllBySerialNumber(String serialNumber);

    void deleteAllByDeviceLibraryIdentifierAndSerialNumber(String deviceLibraryIdentifier, String serialNumber);

    void deleteAllByPushToken(String pushToken);
}

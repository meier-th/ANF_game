package Repositories;


import EntityClasses.FriendsRequest;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface FriendsRequestRepository extends Repository<FriendsRequest, Integer> {

    FriendsRequest save(FriendsRequest request);

    void deleteById(int id);
}

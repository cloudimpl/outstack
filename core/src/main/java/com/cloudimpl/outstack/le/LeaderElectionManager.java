/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.le;

import com.cloudimpl.outstack.collection.CollectionOptions;
import com.cloudimpl.outstack.collection.CollectionProvider;
import com.cloudimpl.outstack.collection.CollectionProvider2;
import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.logger.ILogger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import jdk.jfr.Name;
import reactor.core.publisher.Flux;

/**
 *
 * @author nuwansa
 */
public class LeaderElectionManager {

    private final Map<String, LeaderElection> leaders = new ConcurrentHashMap<>();
    private final FluxMap<String, LeaderElection.LeaderInfo> leaderMap = new FluxMap<>();

    private final CollectionProvider collectionProvider;
    private final CollectionOptions options;
    @Inject
    public LeaderElectionManager(CollectionProvider collectionProvider,@Named("leaderOptions") CollectionOptions options) {
        this.collectionProvider = collectionProvider;
        this.options = options;
    }

    public LeaderElection create(String leaderGroup, String memberId,
            long leaderExpirePeriod,
            ILogger logger) {
        return leaders.computeIfAbsent(leaderGroup+"#"+memberId,
                (name) -> new LeaderElection(leaderGroup, memberId, this.collectionProvider.createHashMap("leaderGroup#"+leaderGroup, options), leaderExpirePeriod, leaderMap,
                        logger));
    }

    public Flux<FluxMap.Event<String, LeaderElection.LeaderInfo>> flux() {
        return leaderMap.flux();
    }
}

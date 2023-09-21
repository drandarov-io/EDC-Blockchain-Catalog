package berlin.tu.ise.extension.blockchain.catalog.listener;

import berlin.tu.ise.extension.blockchain.catalog.listener.model.ReturnObject;
import berlin.tu.ise.extension.blockchain.catalog.listener.model.TokenizedContractDefinitionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.connector.api.management.contractdefinition.ContractDefinitionApiController;
import org.eclipse.edc.connector.contract.spi.event.contractdefinition.ContractDefinitionCreated;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.util.LinkedList;
import java.util.List;

public class BlockchainContractCreator implements EventSubscriber {

    private final Monitor monitor;
    private final String idsWebhookAddress;

    private final ContractDefinitionService contractDefinitionService;

    private final AssetIndex assetIndex;

    private final String edcInterfaceUrl;

    private final ContractDefinitionApiController contractDefinitionApiController;

    public BlockchainContractCreator(Monitor monitor, ContractDefinitionService contractDefinitionService, String idsWebhookAddress, String edcInterfaceUrl, AssetIndex assetIndex, ContractDefinitionApiController contractDefinitionApiController) {
        this.monitor = monitor;
        this.idsWebhookAddress = idsWebhookAddress;
        this.contractDefinitionService = contractDefinitionService;
        this.edcInterfaceUrl = edcInterfaceUrl;
        this.assetIndex = assetIndex;
        this.contractDefinitionApiController = contractDefinitionApiController;
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> event){
        var payload = event.getPayload();
        if (!(payload instanceof ContractDefinitionCreated)) return;
        // the event only returns the contract id, so we need to get the contract object

        ContractDefinitionCreated contractDefinitionCreated;
        contractDefinitionCreated = (ContractDefinitionCreated) payload;
        String contractId = contractDefinitionCreated.getContractDefinitionId();
        monitor.debug("ContractDefinitionCreated event triggered for contractId: " + contractId);

        ContractDefinition contractDefinition = contractDefinitionService.findById(contractId);

        String jsonString = transformToJSON(contractDefinition);
        ReturnObject returnObject = BlockchainHelper.sendToContractSmartContract(jsonString, monitor, edcInterfaceUrl);
        if(returnObject == null) {
            monitor.warning("Something went wrong during the Blockchain Contract Definition creation of the Contract with id " + contractDefinition.getId());
        } else {
            System.out.printf("[%s] Created Contract %s and minted it successfully with the hash: %s\n", this.getClass().getSimpleName(), contractDefinition.getId(), returnObject.getHash());
        }

    }


    private String transformToJSON(ContractDefinition contractDefinition) {

        monitor.info(String.format("[%s] ContractDefinition: for '%s' and '%s' targeting '%s' created in EDC, start now with Blockchain related steps ...", this.getClass().getSimpleName(), contractDefinition.getContractPolicyId(), contractDefinition.getAccessPolicyId(), "not implemented"));

        monitor.info(String.format("[%s] formating POJO to JSON ...", this.getClass().getSimpleName()));

        var contractDefinitionJson = contractDefinitionApiController.getContractDefinition(contractDefinition.getId());

        monitor.warning(String.format("[%s] Contract Definition: %s", this.getClass().getSimpleName(), contractDefinitionJson));

        return String.valueOf(contractDefinitionJson);

    }
}

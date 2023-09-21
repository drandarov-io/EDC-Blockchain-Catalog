package berlin.tu.ise.blockchain.catalog.api;


import berlin.tu.ise.extension.blockchain.catalog.listener.BlockchainHelper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.QueryResponse;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.api.management.asset.v3.AssetApiController;
import org.eclipse.edc.connector.api.management.contractdefinition.ContractDefinitionApiController;
import org.eclipse.edc.connector.api.management.policy.PolicyDefinitionApiController;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractOfferMessage;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.connector.policy.spi.PolicyDefinition;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.validator.spi.JsonObjectValidatorRegistry;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.String.format;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Path("/{a:federatedcatalog|blockchaincatalog}")
public class BlockchainCatalogApiController implements BlockchainCatalogApi {

    private Monitor monitor;

    private String edcBlockchainInterfaceUrl;

    private TypeTransformerRegistry transformerRegistry;
    private JsonObjectValidatorRegistry validatorRegistry;

    public BlockchainCatalogApiController(Monitor monitor, String edcBlockchainInterfaceUrl, TypeTransformerRegistry transformerRegistry, JsonObjectValidatorRegistry validatorRegistry) {
        this.monitor = monitor;
        this.edcBlockchainInterfaceUrl = edcBlockchainInterfaceUrl;
        this.transformerRegistry = transformerRegistry;
        this.validatorRegistry = validatorRegistry;
    }



    @Override
    @POST
    public Collection<ContractOffer> getCatalog(FederatedCatalogCacheQuery federatedCatalogCacheQuery) {

        monitor.info("BlockchainCatalogApiController.getCatalog() called");
        // TODO: send direct request to blockchain?
        List<ContractOffer> offers = new ArrayList<>();
        Criterion criterion = new Criterion("asset:prop:id", "=", "test-document");
        Criterion criterion1 = new Criterion("asset:prop:id", "=", "test-document-64");
        ArrayList<Criterion> queryList = new ArrayList<>();
        queryList.add(criterion);
        queryList.add(criterion1);

        //List<ContractOffer> contractOfferListFromStorage = (List<ContractOffer>) BlockchainHelper.getAllContractDefinitionsFromSmartContract();
        //monitor.info(format("Fetched %d enties from local cataloge storage", contractOfferListFromStorage.size()));

        List<Asset> assetList = BlockchainHelper.getAllAssetsFromSmartContract(edcBlockchainInterfaceUrl, this.monitor, this.transformerRegistry, this.validatorRegistry);
        if (assetList == null) {
            monitor.info(String.format("[%s] No assets found in blockchain. Is something wrong with the edc-interface or is the contract empty?", this.getClass().getSimpleName()));
        } else {
            monitor.info(String.format("[%s] fetched %d Assets from Smart Contract", this.getClass().getSimpleName(), assetList.size()));
        }

        List<PolicyDefinition> policyDefinitionList = BlockchainHelper.getAllPolicyDefinitionsFromSmartContract(edcBlockchainInterfaceUrl, this.monitor, this.transformerRegistry, this.validatorRegistry);
        if (policyDefinitionList == null) {
            monitor.info(String.format("[%s] No policies found in blockchain. Is something wrong with the edc-interface or is the contract empty?", this.getClass().getSimpleName()));
        } else {
            monitor.info(String.format("[%s] fetched %d Policies from Smart Contract", this.getClass().getSimpleName(), policyDefinitionList.size()));
        }

        List<ContractDefinition> contractDefinitionList = BlockchainHelper.getAllContractDefinitionsFromSmartContract(edcBlockchainInterfaceUrl, this.monitor, this.transformerRegistry, this.validatorRegistry);
        if (contractDefinitionList == null) {
            monitor.info(String.format("[%s] No contracts found in blockchain. Is something wrong with the edc-interface or is the contract empty?", this.getClass().getSimpleName()));
        } else {
            monitor.info(String.format("[%s] fetched %d Contracts from Smart Contract", this.getClass().getSimpleName(), contractDefinitionList.size()));
        }

        // HashMap<String, List<ContractOfferDto>> contractDefinitionResponseDtoGroupedBySource = BlockchainHelper.getAllContractDefinitionsFromSmartContractGroupedBySource();

        List<ContractOffer> contractOfferList = new ArrayList<>();

        // iterate over all sources of contractDefinitionResponseDtoGroupedBySource and fetch all contracts for each source

        // iterate over all contracts for a source
        assert contractDefinitionList != null;
        for (ContractDefinition contract : contractDefinitionList) {

            monitor.info(format("[%s] fetching contract %s", this.getClass().getSimpleName(), contract.getId()));


            // TODO: Refactor - connect everything together
            //ContractDefinition contractDefinition = getContractOfferFromContractDefinitionDto(contract, assetList, policyDefinitionResponseDtoList);
            //if (contractOffer != null) {
            //    contractOfferList.add(contractOffer);
            //}

        }



        monitor.info(format("Fetched %d offers from blockchain", contractOfferList.size()));

        return contractOfferList;
    }

    /**
     * Connecting the actual Asset Objects with Policy Objects to the ContractOffer Object
     * @param contract the contract to be created
     * @param assetEntryDtoList the list of all existing assets in the blockchain
     * @param policyDefinitionResponseDtoList the list of all existing policies in the blockchain
     * @return ContractOffer created from combining the Asset and Policy Objects identified by the ids in the ContractDefinitionResponseDto, null if not found
     */
/*
    private ContractOffer getContractOfferFromContractDefinitionDto(ContractOfferDto contract, List<AssetEntryDto> assetEntryDtoList, List<PolicyDefinitionResponseDto> policyDefinitionResponseDtoList) {
        String assetId = String.valueOf(contract.getCriteria().get(0).getOperandRight());
        if (contract.getCriteria().get(0).getOperandRight() instanceof ArrayList) {
            assetId = String.valueOf(((ArrayList<?>) contract.getCriteria().get(0).getOperandRight()).get(0)); // WHAT THE FUCK WARUM MUSS ICH DAS HIER TUN WENN DIE ÜBER DAS EDC DASHBOARD ERSTELLT WERDEN?!?=?!
        }
        String policyId = contract.getContractPolicyId();

        AssetEntryDto assetEntryDto = null;
        PolicyDefinitionResponseDto policyDefinitionResponseDto = null;

        assetEntryDto = getAssetById(assetId, assetEntryDtoList);

        policyDefinitionResponseDto = getPolicyById(policyId, policyDefinitionResponseDtoList);


        if (assetEntryDto == null || policyDefinitionResponseDto == null) {
            monitor.severe(String.format("[%s] Not able to find the Asset with id %s or policy with id %s for the contract %s", this.getClass().getSimpleName(), assetId, policyId, contract.getId()));
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeYearsFronNow = LocalDateTime.now().plusYears(3);
        // convert LocalDateTime to ZonedDateTime, with default system zone id
        ZonedDateTime nowZonedDateTime = now.atZone(ZoneId.systemDefault());
        ZonedDateTime threeYearsFronNowZonedDateTime = threeYearsFronNow.atZone(ZoneId.systemDefault());


        Asset asset = Asset.Builder.newInstance().id(assetEntryDto.getAsset().getId()).properties(assetEntryDto.getAsset().getProperties()).build();
        Policy policy = Policy.Builder.newInstance()
                .target(policyDefinitionResponseDto.getPolicy().getTarget())
                .assignee(policyDefinitionResponseDto.getPolicy().getAssignee())
                .assigner(policyDefinitionResponseDto.getPolicy().getAssigner())
                .prohibitions(policyDefinitionResponseDto.getPolicy().getProhibitions())
                .permissions(policyDefinitionResponseDto.getPolicy().getPermissions())
                .extensibleProperties(policyDefinitionResponseDto.getPolicy().getExtensibleProperties())
                .duties(policyDefinitionResponseDto.getPolicy().getObligations()).build();

        String providerUrl = "http://unavailable:8080";
        if (asset.getProperties().get("asset:prop:originator") != null) {
            providerUrl = asset.getProperties().get("asset:prop:originator").toString();
        }
        return  ContractOffer.Builder.newInstance().assetId(assetEntryDto.getAsset().getId()).policy(policy).id(contract.getId()).providerId(URI.create(providerUrl).toString()).build();
    }

    private PolicyDefinitionResponseDto getPolicyById(String policyId, List<PolicyDefinitionResponseDto> policyDefinitionResponseDtoList) {
        for (PolicyDefinitionResponseDto p: policyDefinitionResponseDtoList) {
            if(p.getId().equals(policyId)) {
                return p;
            }
        }
        return null;
    }

    private AssetEntryDto getAssetById(String assetId, List<AssetEntryDto> assetEntryDtoList) {
        for (AssetEntryDto a: assetEntryDtoList) {
            if(a.getAsset().getId().equals(assetId)) {
                return a;
            }
        }
        return null;
    }

 */
}

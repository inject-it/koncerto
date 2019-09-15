package io.cordacity.koncerto.contract.revocation

import io.cordacity.koncerto.contract.ContractTest
import io.cordacity.koncerto.contract.IDENTITY_A
import io.cordacity.koncerto.contract.IDENTITY_B
import io.cordacity.koncerto.contract.REVOCATION_LOCK
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RevocationLockContractCreationTests : ContractTest() {

    @Test
    fun `On revocation lock creation, the transaction must include the Create command`() {
        services.ledger {
            transaction {
                output(RevocationLockContract.ID, REVOCATION_LOCK)
                fails()
                command(keysOf(IDENTITY_A), RevocationLockContract.Create)
                verifies()
            }
        }
    }

    @Test
    fun `On revocation lock creation, at least one revocation lock state must be created`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Create)
                failsWith(RevocationLockContract.Create.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock creation, the owner of the revocation lock state must sign the transaction`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_B), RevocationLockContract.Create)
                failsWith(RevocationLockContract.Create.CONTRACT_RULE_OUTPUTS)
            }
        }
    }
}
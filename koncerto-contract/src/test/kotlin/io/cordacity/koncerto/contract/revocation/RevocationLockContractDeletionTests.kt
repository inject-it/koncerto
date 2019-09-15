package io.cordacity.koncerto.contract.revocation

import io.cordacity.koncerto.contract.ContractTest
import io.cordacity.koncerto.contract.IDENTITY_A
import io.cordacity.koncerto.contract.IDENTITY_B
import io.cordacity.koncerto.contract.REVOCATION_LOCK
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RevocationLockContractDeletionTests : ContractTest() {

    @Test
    fun `On revocation lock deletion, the transaction must include the Create command`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                fails()
                command(keysOf(IDENTITY_A), RevocationLockContract.Delete)
                verifies()
            }
        }
    }

    @Test
    fun `On revocation lock deletion, only one state must be consumed`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Delete)
                failsWith(RevocationLockContract.Delete.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock deletion, zero states must be created`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                output(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Delete)
                failsWith(RevocationLockContract.Delete.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock deletion, only the owner must sign the transaction`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_B), RevocationLockContract.Delete)
                failsWith(RevocationLockContract.Delete.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
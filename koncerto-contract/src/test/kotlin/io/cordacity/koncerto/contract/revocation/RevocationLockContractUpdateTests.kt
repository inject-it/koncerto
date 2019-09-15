package io.cordacity.koncerto.contract.revocation

import io.cordacity.koncerto.contract.ContractTest
import io.cordacity.koncerto.contract.IDENTITY_A
import io.cordacity.koncerto.contract.IDENTITY_B
import io.cordacity.koncerto.contract.REVOCATION_LOCK
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RevocationLockContractUpdateTests : ContractTest() {

    @Test
    fun `On revocation lock creation, the transaction must include the Update command`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.unlock())
                fails()
                command(keysOf(IDENTITY_A), RevocationLockContract.Update)
                verifies()
            }
        }
    }

    @Test
    fun `On revocation lock updating, only one state must be consumed`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.unlock())
                command(keysOf(IDENTITY_A), RevocationLockContract.Update)
                failsWith(RevocationLockContract.Update.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock updating, only one state must be created`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.unlock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.unlock())
                command(keysOf(IDENTITY_A), RevocationLockContract.Update)
                failsWith(RevocationLockContract.Update.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock updating, the status must change`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                command(keysOf(IDENTITY_A), RevocationLockContract.Update)
                failsWith(RevocationLockContract.Update.CONTRACT_RULE_STATUS)
            }
        }
    }

    @Test
    fun `On revocation lock updating, the owner must not change`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.unlock().copy(owner = IDENTITY_B.party))
                command(keysOf(IDENTITY_A), RevocationLockContract.Update)
                failsWith(RevocationLockContract.Update.CONTRACT_RULE_OWNER)
            }
        }
    }

    @Test
    fun `On revocation lock updating, only the owner must sign the transaction`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK.lock())
                output(RevocationLockContract.ID, REVOCATION_LOCK.unlock())
                command(keysOf(IDENTITY_B), RevocationLockContract.Update)
                failsWith(RevocationLockContract.Update.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
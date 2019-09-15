package io.cordacity.koncerto.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import kotlin.reflect.KClass

interface VerifiedCommand : CommandData {
    fun verify(tx: LedgerTransaction, signers: Set<PublicKey>)
}

internal inline fun <reified T : VerifiedCommand> Contract.verifySingleCommand(tx: LedgerTransaction) {
    val command = tx.commands.requireSingleCommand<T>()
    if (command.value.javaClass.enclosingClass != this.javaClass) {
        throw IllegalArgumentException("Unknown contract command.")
    }
    command.value.verify(tx, command.signers.toSet())
}

internal val KClass<*>.contractClassName: ContractClassName
    get() {
        return if (!this.isCompanion) {
            throw IllegalArgumentException("Must be called from companion object.")
        } else this.java.enclosingClass.canonicalName
    }
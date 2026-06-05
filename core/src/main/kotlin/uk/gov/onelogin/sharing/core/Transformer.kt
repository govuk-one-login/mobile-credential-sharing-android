package uk.gov.onelogin.sharing.core

/**
 * Functional interface that converts provided [Source] data types to [Target] data types.
 *
 * Implementations of this interface often act as an anti-corruption layer - a context boundary
 * for shifting contexts.
 */
fun interface Transformer<Source, Target> {
    fun transform(source: Source): Target
}

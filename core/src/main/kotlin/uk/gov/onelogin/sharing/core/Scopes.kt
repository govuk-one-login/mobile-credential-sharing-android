package uk.gov.onelogin.sharing.core

/**
 * Scope marker for a single sharing/verification session.
 *
 * Objects scoped to [SharingSessionScope] live for the duration of one `createSession()` call.
 * A new session graph (and all its singletons) is created each time the consumer
 * starts a new journey.
 */
interface SharingSessionScope

interface HolderUiScope

interface VerifierUiScope

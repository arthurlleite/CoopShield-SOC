package com.coopshield.soc.sharedkernel.identity;

/**
 * Perfis de acesso do CoopShield SOC, conforme
 * docs/architecture/roles-permissions.md. Compartilhado entre os modulos
 * identity e accesscontrol para evitar duas fontes de verdade sobre perfis.
 */
public enum Role {
    SOC_ANALYST,
    SOC_MANAGER,
    EMPLOYEE,
    BRANCH_MANAGER,
    IT_ADMIN,
    AUDITOR
}

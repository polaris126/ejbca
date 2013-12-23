/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ui.cli.infrastructure.parameter.enums;

/**
 * @version $Id$
 *
 */
public enum StandaloneMode {
    ALLOW(true), FORBID(false);

    private final boolean allowStandalone;

    private StandaloneMode(boolean allowStandalone) {
        this.allowStandalone = allowStandalone;
    }

    public boolean isStandAlone() {
        return allowStandalone;
    }
}

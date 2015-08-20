#
# Copyright (c) 2011-2014 NEC Corporation
# All rights reserved.
# 
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this
# distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
#

##
## Common configurations to build native executable.
##

include ../config.mk
include $(BLDDIR)/exec-defs.mk

# Don't apply configurations for runtime environment.
OPENSSL_RUNPATH		:= $(OPENSSL_LIBDIR)

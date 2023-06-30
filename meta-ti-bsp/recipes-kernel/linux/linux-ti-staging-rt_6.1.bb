require linux-ti-staging_6.1.bb

# Look in the generic major.minor directory for files
# This will have priority over generic non-rt path
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-6.1:"

BRANCH = "ti-rt-linux-6.1.y"

SRCREV = "11eab69ca57cdcb02cd5cd1bcb1d722724b581dd"
PV = "6.1.33+git${SRCPV}"

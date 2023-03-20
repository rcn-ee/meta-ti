# Use TI SECDEV for signing
inherit ti-secdev

EXTRA_OEMAKE:append:k3 = "${@ ' CFG_CONSOLE_UART='+ d.getVar('OPTEE_K3_USART') if d.getVar('OPTEE_K3_USART') else ''}"

do_compile:append:k3() {
    ( cd ${B}/core/; \
        cp tee-pager_v2.bin ${B}/bl32.bin; \
        cp tee.elf ${B}/bl32.elf; \
    )
}

# Signing procedure for legacy HS devices
optee_sign_legacyhs() {
    ( cd ${B}/core/; \
        ${TI_SECURE_DEV_PKG}/scripts/secure-binary-image.sh tee.bin tee.bin.signed; \
        normfl=`echo ${OPTEEFLAVOR} | tr "_" "-"`
        mv tee.bin.signed ${B}/$normfl.optee; \
    )

    if [ "${OPTEEPAGER}" = "y" ]; then
        oe_runmake -C ${S} clean
        oe_runmake -C ${S} all CFG_TEE_TA_LOG_LEVEL=0 CFG_WITH_PAGER=y
        ( cd ${B}/core/; \
            ${TI_SECURE_DEV_PKG}/scripts/secure-binary-image.sh tee.bin tee.bin.signed; \
            normfl=`echo ${OPTEEFLAVOR} | tr "_" "-"`
            mv tee.bin.signed ${B}/$normfl-pager.optee; \
        )
    fi
}

do_compile:append:ti43x() {
    optee_sign_legacyhs
}

do_compile:append:dra7xx() {
    optee_sign_legacyhs
}

# Signing procedure for K3 devices
do_compile:append:k3() {
    ${TI_SECURE_DEV_PKG}/scripts/secure-binary-image.sh ${B}/core/tee-pager_v2.bin ${B}/bl32.bin
    cp ${B}/core/tee.elf ${B}/bl32.elf
}

do_install:append:ti-soc() {
    install -m 644 ${B}/*.optee ${D}${nonarch_base_libdir}/firmware/ || true
    install -m 644 ${B}/bl32.bin ${D}${nonarch_base_libdir}/firmware/ || true
    install -m 644 ${B}/bl32.elf ${D}${nonarch_base_libdir}/firmware/ || true
}

optee_deploy_legacyhs() {
    cd ${DEPLOYDIR}/
    for f in optee/*.optee; do
        ln -sf $f ${DEPLOYDIR}/
    done
}

do_deploy:append:ti43x() {
    optee_deploy_legacyhs
}

do_deploy:append:dra7xx() {
    optee_deploy_legacyhs
}

do_deploy:append:k3() {
    ln -sf optee/bl32.bin ${DEPLOYDIR}/
    ln -sf optee/bl32.elf ${DEPLOYDIR}/
}

# This is needed for bl32.elf
INSANE_SKIP:${PN}:append:k3 = " textrel"

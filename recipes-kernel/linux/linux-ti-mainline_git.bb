SECTION = "kernel"
SUMMARY = "Mainline Linux kernel for TI devices (with ti-upstream-tools)"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel

DEFCONFIG_BUILDER = "${WORKDIR}/ti-upstream-tools/config/defconfig_builder.sh"
require recipes-kernel/linux/setup-defconfig.inc
require recipes-kernel/linux/kernel-rdepends.inc

DEPENDS += "gmp-native"

KERNEL_EXTRA_ARGS += "LOADADDR=${UBOOT_ENTRYPOINT}"

S = "${WORKDIR}/git"

BRANCH = "master"
TOOLS_BRANCH = "master"

# 5.12 Mainline version
SRCREV = "9f4ad9e425a1d3b6a34617b8ea226d56a119a717"
PV = "5.12+git${SRCPV}"

# Append to the MACHINE_KERNEL_PR so that a new SRCREV will cause a rebuild
MACHINE_KERNEL_PR_append = "a"
PR = "${MACHINE_KERNEL_PR}"

KERNEL_GIT_URI = "git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git"
KERNEL_GIT_PROTOCOL = "git"
SRC_URI += " \
    ${KERNEL_GIT_URI};protocol=${KERNEL_GIT_PROTOCOL};branch=${BRANCH};name=linux \
    git://git.ti.com/ti-linux-kernel/ti-upstream-tools.git;branch=${TOOLS_BRANCH};protocol=${KERNEL_GIT_PROTOCOL};name=ti-upstream-tools;destsuffix=ti-upstream-tools \
    file://defconfig \
"

SRCREV_ti-upstream-tools = "d59b7471d99b806e3dc22342c8f42d5bb33f8cce"
SRCREV_FORMAT = "linux"

KERNEL_DEVICETREE = ""

kernel_do_compile_append() {
	oe_runmake dtbs CC="${KERNEL_CC} $cc_extra " LD="${KERNEL_LD}" ${KERNEL_EXTRA_ARGS}
	oe_runmake -C ${WORKDIR}/ti-upstream-tools LINUX=${S} DTC=${B}/scripts/dtc/dtc O=${B} CC="${KERNEL_CC} $cc_extra " LD="${KERNEL_LD}" ${KERNEL_EXTRA_ARGS}
}

do_install_append() {
	for dtbf in `find arch/${ARCH}/boot/dts/ \( -name '*.dtb' -or -name '*.dtbo' \)`; do
		dtb="$dtbf"
		dtb_ext=${dtb##*.}
		dtb_base_name=`basename $dtb .$dtb_ext`
		dtb_path=`get_real_dtb_path_in_kernel "$dtb"`
		install -m 0644 $dtbf ${D}/${KERNEL_IMAGEDEST}/$dtb_base_name.$dtb_ext
	done
}

do_deploy_append() {
	for dtbf in `find arch/${ARCH}/boot/dts/ \( -name '*.dtb' -or -name '*.dtbo' \) -printf '%P\n'`; do
		dtb="$dtbf"
		dtb_ext=${dtb##*.}
		dtb_base_name=`basename $dtb .$dtb_ext`
		dtb_dir=`dirname $dtb`
		install -d ${DEPLOYDIR}
		install -m 0644 ${D}/${KERNEL_IMAGEDEST}/$dtb_base_name.$dtb_ext ${DEPLOYDIR}/$dtb_base_name.$dtb_ext
	done
}

do_shared_workdir_prepend() {
	cd ${B}
	echo >> Module.symvers
}

FILES_${KERNEL_PACKAGE_NAME}-devicetree += "/${KERNEL_IMAGEDEST}/*.itb"

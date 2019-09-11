inherit kernel-resin deploy
FILESEXTRAPATHS_append := ":${THISDIR}/${PN}"

SRC_URI_append = " \
    file://0002-NFLX-2019-001-SACK-Panic.patch \
    file://0003-NFLX-2019-001-SACK-Panic-for-lteq-4.14.patch \
    file://0004-NFLX-2019-001-SACK-Slowness.patch \
    file://0005-NFLX-2019-001-Resour-Consump-Low-MSS.patch \
    file://0006-NFLX-2019-001-Resour-Consump-Low-MSS.patch \
    "

RESIN_CONFIGS_append = " compat"

RESIN_CONFIGS[compat] = " \
    CONFIG_COMPAT=y \
    "
RESIN_CONFIGS_remove = "brcmfmac"

TEGRA_INITRAMFS_INITRD = "0"

KERNEL_ROOTSPEC = "\${resin_kernel_root} ro rootwait" 
KERNEL_ROOTSPEC_FLASHER = "root=/dev/mmcblk1p11 ro rootwait" 

generate_extlinux_conf() {
    install -d ${D}/${KERNEL_IMAGEDEST}/extlinux
    rm -f ${D}/${KERNEL_IMAGEDEST}/extlinux/extlinux.conf
    rm -f ${D}/${KERNEL_IMAGEDEST}/extlinux/extlinux.conf_flasher
    kernelRootspec="${KERNEL_ROOTSPEC}" ; cat >${D}/${KERNEL_IMAGEDEST}/extlinux/extlinux.conf << EOF
DEFAULT primary
TIMEOUT 30
MENU TITLE Boot Options
LABEL primary
      MENU LABEL primary ${KERNEL_IMAGETYPE}
      LINUX /${KERNEL_IMAGETYPE}
      APPEND ${KERNEL_ARGS} ${kernelRootspec}
EOF
    kernelRootspec="${KERNEL_ROOTSPEC_FLASHER}" ; cat >${D}/${KERNEL_IMAGEDEST}/extlinux/extlinux.conf_flasher << EOF
DEFAULT primary
TIMEOUT 30
MENU TITLE Boot Options
LABEL primary
      MENU LABEL primary ${KERNEL_IMAGETYPE}
      LINUX /${KERNEL_IMAGETYPE}
      APPEND ${KERNEL_ARGS} ${kernelRootspec}
EOF
}

do_deploy_append() {
    mkdir -p "${DEPLOYDIR}"
    install -m 0600 "${D}/boot/extlinux/extlinux.conf" "${DEPLOYDIR}"
    install -m 0600 "${D}/boot/extlinux/extlinux.conf_flasher" "${DEPLOYDIR}"
}

UBOOT_KCONFIG_SUPPORT = "1"

inherit resin-u-boot

RESIN_BOOT_PART = "0xa"
RESIN_DEFAULT_ROOT_PART = "0xb"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append = " file://0001-Integrate-resin-u-boot.patch"
SRC_URI_append_jetson-tx1 = " file://0002-jetson-tx1-Enable-CONFIG_CMD_SETEXPR.patch"

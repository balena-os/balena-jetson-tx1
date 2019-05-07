SUMMARY = "Prepare bsp binaries for flashing"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${RESIN_COREBASE}/COPYING.Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

DEPENDS = "virtual/bootloader virtual/kernel tegra-binaries"

inherit deploy
SRC_URI = " \
    file://flash.xml \
    file://partition_specification.txt \
    "

SHARED = "${TMPDIR}/work-shared/L4T-${SOC_FAMILY}-${PV}-${PR}/Linux_for_Tegra"
B = "${WORKDIR}/build"
S = "${WORKDIR}"

DTB_jetson-tx1 = "${SHARED}/kernel/dtb/tegra210-jetson-tx1-p2597-2180-a01-devkit.dtb"

BINARY_INSTALL_PATH = "/opt/tegra-binaries"

do_configure() {
    dtb_name=$(basename ${DTB} | cut -d '.' -f 1)
    sed -i -e "s/\[DTB_NAME\]/${dtb_name}/g" ${WORKDIR}/flash.xml
    sed -i -e "s/\[DTB_NAME\]/${dtb_name}/g" ${WORKDIR}/partition_specification.txt
}

do_compile() {
    tegraflash="${SHARED}/bootloader/tegraflash.py"

    files=" \
      ${SHARED}/bootloader/t210ref/BCT/P2180_A00_LP4_DSC_204Mhz.cfg \
      ${SHARED}/bootloader/t210ref/cboot.bin \
      ${SHARED}/bootloader/t210ref/cfg/board_config_p2597-devkit.xml \
      ${SHARED}/bootloader/t210ref/nvtboot.bin \
      ${SHARED}/bootloader/t210ref/warmboot.bin \
      ${SHARED}/bootloader/nvtboot_recovery.bin \
      ${SHARED}/bootloader/nvtboot_cpu.bin \
      ${SHARED}/bootloader/bpmp.bin \
      ${SHARED}/bootloader/tos.img \
      ${SHARED}/bootloader/eks.img \
      ${DTB}\
      "
    
    for file in $files; do
			cp $file ${B}
    done

    cp ${WORKDIR}/flash.xml ${B}

    ${tegraflash} --bl cboot.bin --bct  P2180_A00_LP4_DSC_204Mhz.cfg --odmdata 0x84000 --bldtb tegra210-jetson-tx1-p2597-2180-a01-devkit.dtb --applet nvtboot_recovery.bin --boardconfig board_config_p2597-devkit.xml --cmd "flash"  --cfg flash.xml --chip 0x21 --keep & export _PID=$! ; wait ${_PID} || true

    mkdir -p ${B}/out
    cp -r ${B}/${_PID}/* ${B}/out
    rm -rf ${B}/${_PID}
}

do_install() {
    install -d ${D}/${BINARY_INSTALL_PATH}

    files=$(cat ${WORKDIR}/partition_specification.txt | grep -v :u-boot)

    for file in $files; do
        file_name=$(echo $file | cut -d ':' -f 2)
        cp ${B}/out/$file_name ${D}/${BINARY_INSTALL_PATH}
    done

    cp ${WORKDIR}/partition_specification.txt ${D}/${BINARY_INSTALL_PATH}

    cp ${DEPLOY_DIR_IMAGE}/u-boot-dtb.bin ${D}/${BINARY_INSTALL_PATH}
}

do_deploy() {
    mkdir -p ${DEPLOYDIR}/$(basename ${BINARY_INSTALL_PATH})
    cp -r ${D}/${BINARY_INSTALL_PATH}/* ${DEPLOYDIR}/$(basename ${BINARY_INSTALL_PATH})
}

FILES_${PN} += "${BINARY_INSTALL_PATH}"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_configure[depends] += "tegra-binaries:do_preconfigure"
do_compile[depends] += "virtual/kernel:do_deploy"
do_install[depends] += "virtual/kernel:do_deploy"
do_populate_lic[depends] += "tegra-binaries:do_unpack"

addtask do_deploy before do_package after do_install

#!/system/bin/sh

# 从build.prop文件读取prop的值是否为1
function cat_prop_is_1()
{
    prop="$1"
    status=`grep $prop /system/build.prop|cut -d '=' -f2`
    if [ "$status" = "1" ]; then
        echo 1;
        exit 0
    fi


    if [[ -d "$MAGISK_PATH" ]] && [[ -f "$MAGISK_PATH/system.prop" ]]
    then
        status=`grep $prop $MAGISK_PATH/system.prop | cut -d '=' -f2`
    fi

    if [ "$status" = 1 ]; then
        echo 1;
    else
        echo 0
    fi;
}

# 从build.prop文件读取prop的值是否为0
function cat_prop_is_0()
{
    prop="$1"
    status=`grep $prop /system/build.prop|cut -d '=' -f2`
    if [ "$status" = "1" ]; then
        echo 0;
        exit 0
    fi


    if [[ -d "$MAGISK_PATH" ]] && [[ -f "$MAGISK_PATH/system.prop" ]]
    then
        status=`grep $prop $MAGISK_PATH/system.prop | cut -d '=' -f2`
    fi

    if [ "$status" = 1 ]; then
        echo 0;
    else
        echo 1
    fi;
}

function magisk_set_system_prop() {
    if [[ -d "$MAGISK_PATH" ]];
    then
        echo "你已安装Magisk，本次修改将通过操作进行"
        $BUSYBOX sed -i "/$1=/"d "$MAGISK_PATH/system.prop"
        $BUSYBOX sed -i "\$a$1=$2" "$MAGISK_PATH/system.prop"
        setprop $1 $2 2> /dev/null
        return 1
    fi;
    return 0
}

function set_system_prop() {
    local prop=$1
    local state=$2

    local path="/system/build.prop"
    if [[ -f /vendor/build.prop ]] && [[ -n `cat /vendor/build.prop | grep $prop=` ]]
    then
        local path="/vendor/build.prop"
    fi

    echo '使用本功能，需要解锁system分区，否则修改无效！'
    echo '系统自带的ROOT可能无法使用本功能'

    echo 'Step1.挂载/system为读写'

    $BUSYBOX mount -o rw,remount /system 2> /dev/null
    mount -o rw,remount /system 2> /dev/null
    $BUSYBOX mount -o remount,rw /dev/block/bootdevice/by-name/system /system 2> /dev/null
    mount -o remount,rw /dev/block/bootdevice/by-name/system /system 2> /dev/null

    busybox mount -o rw,remount /vendor 2> /dev/null
    mount -o rw,remount /vendor 2> /dev/null

    $BUSYBOX sed "/$prop=/"d $path > /cache/build.prop
    $BUSYBOX sed -i "\$a$prop=$state" /cache/build.prop
    echo "Step2.修改$prop=$state"

    echo 'Step3.写入文件'
    cp /cache/build.prop $path
    chmod 0755 $path

    echo 'Step4.删除临时文件'
    rm /cache/build.prop
    sync

    echo ''
    echo '重启后生效！'
}
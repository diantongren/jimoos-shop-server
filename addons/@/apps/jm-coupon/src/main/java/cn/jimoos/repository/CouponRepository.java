package cn.jimoos.repository;

import cn.jimoos.common.exception.BussException;
import cn.jimoos.dao.CouponMapper;
import cn.jimoos.dao.CouponRecordMapper;
import cn.jimoos.entity.CouponEntity;
import cn.jimoos.error.CouponError;
import cn.jimoos.model.Coupon;
import cn.jimoos.model.CouponRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author :keepcleargas
 * @date :2021-03-25 15:31.
 */
@Repository
public class CouponRepository {
    @Resource
    CouponMapper couponMapper;
    @Resource
    CouponRecordMapper couponRecordMapper;

    public CouponEntity findById(Long couponId) {
        return wrapper(couponMapper.selectByPrimaryKey(couponId), false);
    }

    public CouponEntity findByCode(String code) {
        return wrapper(couponMapper.findFirstByCode(code), false);
    }

    public CouponRecord findRecordById(Long couponRecordId) {
        CouponRecord couponRecord = couponRecordMapper.selectByPrimaryKey(couponRecordId);

        if (couponRecord != null && !couponRecord.getDeleted()) {
            return couponRecord;
        } else {
            return null;
        }
    }

    /**
     * 保存 CouponEntity信息
     *
     * @param couponEntity Coupon Entity
     */
    public void save(CouponEntity couponEntity) {
        if (couponEntity.getId() != null && couponEntity.getId() > 0) {
            couponMapper.updateByPrimaryKey(couponEntity);
        } else {
            couponMapper.insert(couponEntity);
        }
    }

    public void saveRecord(CouponRecord couponRecord) {
        couponRecordMapper.updateByPrimaryKey(couponRecord);
    }

    public CouponRecord findRecord(Long couponId, Long userId) {
        return couponRecordMapper.findOneByCouponIdAndUserId(couponId, userId);
    }

    /**
     * @Coupon的 entity wrapper方法
     */
    private CouponEntity wrapper(Coupon coupon, boolean skipRepo) {
        if (coupon != null) {
            if (skipRepo) {
                return (CouponEntity) coupon;
            }
            CouponEntity couponEntity = new CouponEntity(this);
            BeanUtils.copyProperties(coupon, couponEntity);
            return couponEntity;
        }
        return null;
    }

    /**
     * 保存 发放记录
     *
     * @param couponEntity 优惠券对象
     */
    public void saveRecords(CouponEntity couponEntity) throws BussException {
        // modify 2021-7-22 09:49:07 扣除优惠券时 做库存的二次校验
        int i = couponMapper.reduceNum(couponEntity.getId());
        if (i <= 0) {
            throw new BussException(CouponError.COUPON_NOT_ENOUGH);
        }
        if (!CollectionUtils.isEmpty(couponEntity.getCouponRecordInputs())) {
            couponRecordMapper.batchInsert(couponEntity.getCouponRecordInputs());
        }
    }

    /**
     * 查询优惠券${couponId}的领取记录统计
     *
     * @param couponId 优惠券 ID
     * @return
     */
    public Long countRecords(Long couponId) {
        return couponRecordMapper.countByCouponId(couponId);
    }

    /**
     * 查询某优惠券关联订单的总金额
     * @param id
     * @return
     */
    public BigDecimal queryAssociatedOrderAmount(Long id) {
        return couponRecordMapper.queryAssociatedOrderAmount(id);
    }
}

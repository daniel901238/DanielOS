using UnityEngine;

namespace ProjectRelay.Gameplay
{
    public class LoadoutWeightSystem : MonoBehaviour
    {
        [Header("Weight")]
        [SerializeField] private float carriedWeightKg = 0f;
        [SerializeField] private float safeWeightKg = 20f;
        [SerializeField] private float hardLimitWeightKg = 50f;

        [Header("Movement Penalty")]
        [SerializeField] private float maxMovePenalty = 0.45f;

        [Header("Balance")]
        [SerializeField] private float baseFallChancePerSecond = 0.01f;
        [SerializeField] private float extraFallChanceAtHardLimit = 0.15f;

        public float CarriedWeightKg => carriedWeightKg;
        public float WeightRatio => hardLimitWeightKg <= 0 ? 0 : carriedWeightKg / hardLimitWeightKg;

        public float GetMoveSpeedMultiplier()
        {
            float overload = Mathf.InverseLerp(safeWeightKg, hardLimitWeightKg, carriedWeightKg);
            return 1f - (maxMovePenalty * overload);
        }

        public float GetFallChancePerSecond()
        {
            float overload = Mathf.InverseLerp(safeWeightKg, hardLimitWeightKg, carriedWeightKg);
            return baseFallChancePerSecond + (extraFallChanceAtHardLimit * overload);
        }

        public void AddWeight(float kg)
        {
            carriedWeightKg = Mathf.Clamp(carriedWeightKg + kg, 0f, hardLimitWeightKg);
        }

        public void RemoveWeight(float kg)
        {
            carriedWeightKg = Mathf.Clamp(carriedWeightKg - kg, 0f, hardLimitWeightKg);
        }
    }
}

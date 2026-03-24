using UnityEngine;

namespace ProjectRelay.World
{
    public enum TerrainRiskLevel
    {
        Safe,
        Caution,
        Dangerous
    }

    [RequireComponent(typeof(Collider))]
    public class TerrainRiskZone : MonoBehaviour
    {
        [SerializeField] private TerrainRiskLevel riskLevel = TerrainRiskLevel.Safe;
        [SerializeField] private float staminaDrainMultiplier = 1f;
        [SerializeField] private float slipChanceBonus = 0f;

        public TerrainRiskLevel RiskLevel => riskLevel;
        public float StaminaDrainMultiplier => staminaDrainMultiplier;
        public float SlipChanceBonus => slipChanceBonus;

        private void Reset()
        {
            Collider c = GetComponent<Collider>();
            c.isTrigger = true;
        }
    }
}

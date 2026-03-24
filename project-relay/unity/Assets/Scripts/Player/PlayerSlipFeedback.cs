using System.Collections;
using ProjectRelay.World;
using UnityEngine;

namespace ProjectRelay.Player
{
    public class PlayerSlipFeedback : MonoBehaviour
    {
        [Header("References")]
        [SerializeField] private PlayerLocomotion playerLocomotion;
        [SerializeField] private PlayerTerrainState terrainState;
        [SerializeField] private Transform cameraTransform;
        [SerializeField] private AudioSource sfxSource;

        [Header("Slip SFX by Risk")]
        [SerializeField] private AudioClip slipSafeClip;
        [SerializeField] private AudioClip slipCautionClip;
        [SerializeField] private AudioClip slipDangerClip;

        [Header("Zone Enter Warning SFX")]
        [SerializeField] private AudioClip cautionEnterClip;
        [SerializeField] private AudioClip dangerEnterClip;

        [Header("Camera Shake")]
        [SerializeField] private float shakeDuration = 0.18f;
        [SerializeField] private float shakeMagnitude = 0.08f;

        [Header("Haptic")]
        [SerializeField] private bool useHapticOnMobile = true;

        private Vector3 _cameraLocalStartPos;
        private Coroutine _shakeRoutine;

        private void Awake()
        {
            if (playerLocomotion == null)
                playerLocomotion = GetComponent<PlayerLocomotion>();

            if (terrainState == null)
                terrainState = GetComponent<PlayerTerrainState>();

            if (cameraTransform != null)
                _cameraLocalStartPos = cameraTransform.localPosition;
        }

        private void OnEnable()
        {
            if (playerLocomotion != null)
                playerLocomotion.Slipped += HandleSlip;

            if (terrainState != null)
                terrainState.RiskLevelChanged += HandleRiskLevelChanged;
        }

        private void OnDisable()
        {
            if (playerLocomotion != null)
                playerLocomotion.Slipped -= HandleSlip;

            if (terrainState != null)
                terrainState.RiskLevelChanged -= HandleRiskLevelChanged;
        }

        private void HandleSlip()
        {
            PlaySlipSfxByRisk();
            TriggerHaptic();
            TriggerCameraShake();
        }

        private void HandleRiskLevelChanged(TerrainRiskLevel riskLevel)
        {
            if (sfxSource == null) return;

            if (riskLevel == TerrainRiskLevel.Caution && cautionEnterClip != null)
                sfxSource.PlayOneShot(cautionEnterClip);
            else if (riskLevel == TerrainRiskLevel.Dangerous && dangerEnterClip != null)
                sfxSource.PlayOneShot(dangerEnterClip);
        }

        private void PlaySlipSfxByRisk()
        {
            if (sfxSource == null) return;

            TerrainRiskLevel risk = terrainState != null ? terrainState.CurrentRiskLevel : TerrainRiskLevel.Safe;
            AudioClip chosen = slipSafeClip;

            if (risk == TerrainRiskLevel.Caution && slipCautionClip != null)
                chosen = slipCautionClip;
            else if (risk == TerrainRiskLevel.Dangerous && slipDangerClip != null)
                chosen = slipDangerClip;

            if (chosen != null)
                sfxSource.PlayOneShot(chosen);
        }

        private void TriggerHaptic()
        {
#if UNITY_ANDROID || UNITY_IOS
            if (useHapticOnMobile)
                Handheld.Vibrate();
#endif
        }

        private void TriggerCameraShake()
        {
            if (cameraTransform == null) return;

            if (_shakeRoutine != null)
                StopCoroutine(_shakeRoutine);

            _shakeRoutine = StartCoroutine(CoShake());
        }

        private IEnumerator CoShake()
        {
            float t = 0f;
            while (t < shakeDuration)
            {
                t += Time.deltaTime;
                Vector3 offset = Random.insideUnitSphere * shakeMagnitude;
                offset.z = 0f;
                cameraTransform.localPosition = _cameraLocalStartPos + offset;
                yield return null;
            }

            cameraTransform.localPosition = _cameraLocalStartPos;
            _shakeRoutine = null;
        }
    }
}
